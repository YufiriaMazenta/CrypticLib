package crypticlib.database;

import crypticlib.database.annotation.Field;
import crypticlib.database.annotation.Table;
import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.connection.JdbcConnectionSource;
import crypticlib.database.connection.PooledConnectionSource;
import crypticlib.database.dao.Dao;
import crypticlib.database.dao.DaoManager;
import crypticlib.database.dialect.DatabaseDialect;
import crypticlib.database.dialect.H2Dialect;
import crypticlib.database.dialect.MysqlDialect;
import crypticlib.database.dialect.PostgresqlDialect;
import crypticlib.database.dialect.SqliteDialect;
import crypticlib.database.table.TableInfo;
import crypticlib.database.table.TableUtils;
import crypticlib.database.transaction.TransactionManager;
import org.junit.jupiter.api.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Database ORM 测试")
public class DatabaseTest {

    private static ConnectionSource source;
    private static Dao<TestUser> dao;

    @BeforeAll
    static void setup() throws SQLException {
        TestDatabases.assumeAvailable();
        source = TestDatabases.source("test");
        TestDatabases.resetTable(source, TestUser.class);
        dao = DaoManager.createDao(source, TestUser.class);
    }

    @AfterAll
    static void teardown() {
        // 后端不可用时 @BeforeAll 会被跳过，这里必须容忍连接源未创建的情况
        if (source != null) {
            source.close();
        }
    }

    @BeforeEach
    void clearTable() throws SQLException {
        TableUtils.clearTable(source, TestUser.class);
    }

    // ========== 基础 CRUD ==========

    @Test
    @DisplayName("插入并查询")
    void testInsertAndQuery() throws SQLException {
        TestUser user = new TestUser("Steve", 20, 100.0);
        int result = dao.create(user);

        assertEquals(1, result);
        assertTrue(user.getId() > 0, "the generated id should be written back");

        TestUser found = dao.queryForId(user.getId());
        assertNotNull(found);
        assertEquals("Steve", found.getUsername());
        assertEquals(20, found.getAge());
        assertEquals(100.0, found.getBalance(), 0.001);
    }

    @Test
    @DisplayName("查询不存在的 ID 返回 null")
    void testQueryNonExistentId() throws SQLException {
        TestUser found = dao.queryForId(99999L);
        assertNull(found);
    }

    @Test
    @DisplayName("查询所有")
    void testQueryForAll() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 300.0));

        List<TestUser> users = dao.queryForAll();
        assertEquals(3, users.size());
    }

    @Test
    @DisplayName("空表查询所有返回空列表")
    void testQueryForAllEmpty() throws SQLException {
        List<TestUser> users = dao.queryForAll();
        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    @Test
    @DisplayName("更新")
    void testUpdate() throws SQLException {
        TestUser user = new TestUser("Steve", 20, 100.0);
        dao.create(user);

        user.setBalance(999.0);
        user.setAge(21);
        int updated = dao.update(user);
        assertEquals(1, updated);

        TestUser found = dao.queryForId(user.getId());
        assertEquals(999.0, found.getBalance(), 0.001);
        assertEquals(21, found.getAge());
        assertEquals("Steve", found.getUsername());
    }

    @Test
    @DisplayName("删除")
    void testDelete() throws SQLException {
        TestUser user = new TestUser("Steve", 20, 100.0);
        dao.create(user);

        int deleted = dao.delete(user);
        assertEquals(1, deleted);

        TestUser found = dao.queryForId(user.getId());
        assertNull(found);
    }

    @Test
    @DisplayName("删除后再查询返回 null")
    void testDeleteThenQuery() throws SQLException {
        TestUser user = new TestUser("Steve", 20, 100.0);
        dao.create(user);
        long id = user.getId();

        dao.delete(user);
        assertNull(dao.queryForId(id));

        // 再插入同名用户，ID 应该不同
        TestUser user2 = new TestUser("Steve", 20, 100.0);
        dao.create(user2);
        assertNotEquals(id, user2.getId());
    }

    // ========== replace ==========

    @Test
    @DisplayName("replace 插入新记录")
    void testReplaceInsert() throws SQLException {
        TestUser user = new TestUser("Steve", 20, 100.0);
        int result = dao.replace(user);

        assertEquals(1, result);
        assertTrue(user.getId() > 0);

        TestUser found = dao.queryForId(user.getId());
        assertNotNull(found);
        assertEquals("Steve", found.getUsername());
    }

    @Test
    @DisplayName("replace 更新已有记录")
    void testReplaceUpdate() throws SQLException {
        TestUser user = new TestUser("Steve", 20, 100.0);
        dao.create(user);
        long originalId = user.getId();

        user.setBalance(999.0);
        dao.replace(user);

        TestUser found = dao.queryForId(originalId);
        assertNotNull(found);
        assertEquals(999.0, found.getBalance(), 0.001);
        assertEquals(originalId, found.getId(), "replace should keep the original id");
    }

    // ========== QueryBuilder ==========

    @Test
    @DisplayName("QueryBuilder equals 查询")
    void testQueryEquals() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.equals("username", "Steve"))
            .query();
        assertEquals(1, results.size());
        assertEquals("Steve", results.get(0).getUsername());
    }

    @Test
    @DisplayName("QueryBuilder notEquals 查询")
    void testQueryNotEquals() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 300.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.notEquals("username", "Steve"))
            .query();
        assertEquals(2, results.size());
        assertTrue(results.stream().noneMatch(u -> u.getUsername().equals("Steve")));
    }

    @Test
    @DisplayName("QueryBuilder greaterThan 查询")
    void testQueryGreaterThan() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.greaterThan("age", 20))
            .query();
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Alex")));
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Bob")));
    }

    @Test
    @DisplayName("QueryBuilder lessThanOrEquals 查询")
    void testQueryLessThanOrEquals() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.lessThanOrEquals("age", 25))
            .query();
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Steve")));
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Alex")));
    }

    @Test
    @DisplayName("QueryBuilder LIKE 查询")
    void testQueryLike() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("stephen", 30, 300.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.like("username", "%ph%"))
            .query();
        assertEquals(1, results.size());
        assertEquals("stephen", results.get(0).getUsername());
    }

    @Test
    @DisplayName("QueryBuilder IN 查询")
    void testQueryIn() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.in("username", "Steve", "Bob"))
            .query();
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Steve")));
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Bob")));
    }

    @Test
    @DisplayName("QueryBuilder IS NOT NULL 查询")
    void testQueryIsNotNull() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.isNotNull("username"))
            .query();
        assertEquals(1, results.size());
    }

    @Test
    @DisplayName("QueryBuilder OR 条件")
    void testQueryOr() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where
                .equals("username", "Steve")
                .or()
                .equals("username", "Bob"))
            .query();
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Steve")));
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Bob")));
    }

    @Test
    @DisplayName("QueryBuilder 排序升序")
    void testQueryOrderByAsc() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.greaterThan("balance", 0.0))
            .orderBy("balance", true)
            .query();
        assertEquals(3, results.size());
        assertEquals("Bob", results.get(0).getUsername());
        assertEquals("Steve", results.get(1).getUsername());
        assertEquals("Alex", results.get(2).getUsername());
    }

    @Test
    @DisplayName("QueryBuilder 排序降序 + 限制")
    void testQueryOrderByDescLimit() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.greaterThan("balance", 0.0))
            .orderBy("balance", false)
            .limit(2)
            .query();
        assertEquals(2, results.size());
        assertEquals("Alex", results.get(0).getUsername());
        assertEquals("Steve", results.get(1).getUsername());
    }

    @Test
    @DisplayName("QueryBuilder 偏移量")
    void testQueryOffset() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.greaterThan("balance", 0.0))
            .orderBy("balance", false)
            .limit(2)
            .offset(1)
            .query();
        assertEquals(2, results.size());
        assertEquals("Steve", results.get(0).getUsername());
        assertEquals("Bob", results.get(1).getUsername());
    }

    @Test
    @DisplayName("QueryBuilder 多条件 AND")
    void testQueryMultipleAnd() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where
                .greaterThanOrEquals("age", 25)
                .and()
                .lessThan("balance", 200.0))
            .query();
        assertEquals(1, results.size());
        assertEquals("Bob", results.get(0).getUsername());
    }

    @Test
    @DisplayName("QueryBuilder 无结果查询返回空列表")
    void testQueryNoResults() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));

        List<TestUser> results = dao.queryBuilder()
            .where(where -> where.equals("username", "NotExist"))
            .query();
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // ========== UpdateBuilder ==========

    @Test
    @DisplayName("UpdateBuilder 条件更新")
    void testUpdateBuilder() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 300.0));

        int updated = dao.updateBuilder()
            .set("balance", 0.0)
            .where(where -> where.greaterThan("balance", 150.0))
            .update();

        assertEquals(2, updated);

        List<TestUser> all = dao.queryForAll();
        for (TestUser user : all) {
            if (user.getUsername().equals("Steve")) {
                assertEquals(100.0, user.getBalance(), 0.001);
            } else {
                assertEquals(0.0, user.getBalance(), 0.001);
            }
        }
    }

    @Test
    @DisplayName("UpdateBuilder 更新多列")
    void testUpdateBuilderMultipleColumns() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));

        int updated = dao.updateBuilder()
            .set("age", 99)
            .set("balance", 9999.0)
            .where(where -> where.equals("username", "Steve"))
            .update();

        assertEquals(1, updated);

        TestUser found = dao.queryForAll().get(0);
        assertEquals(99, found.getAge());
        assertEquals(9999.0, found.getBalance(), 0.001);
    }

    @Test
    @DisplayName("UpdateBuilder 无匹配时不更新")
    void testUpdateBuilderNoMatch() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));

        int updated = dao.updateBuilder()
            .set("balance", 0.0)
            .where(where -> where.equals("username", "NotExist"))
            .update();

        assertEquals(0, updated);

        TestUser found = dao.queryForAll().get(0);
        assertEquals(100.0, found.getBalance(), 0.001);
    }

    // ========== DeleteBuilder ==========

    @Test
    @DisplayName("DeleteBuilder 条件删除")
    void testDeleteBuilder() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 300.0));

        int deleted = dao.deleteBuilder()
            .where(where -> where.lessThan("balance", 200.0))
            .delete();

        assertEquals(1, deleted);

        List<TestUser> remaining = dao.queryForAll();
        assertEquals(2, remaining.size());
    }

    @Test
    @DisplayName("DeleteBuilder 无匹配时不删除")
    void testDeleteBuilderNoMatch() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));

        int deleted = dao.deleteBuilder()
            .where(where -> where.equals("username", "NotExist"))
            .delete();

        assertEquals(0, deleted);
        assertEquals(1, dao.queryForAll().size());
    }

    @Test
    @DisplayName("DeleteBuilder 删除全部")
    void testDeleteBuilderAll() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));

        int deleted = dao.deleteBuilder()
            .where(where -> where.greaterThan("id", 0L))
            .delete();

        assertEquals(2, deleted);
        assertTrue(dao.queryForAll().isEmpty());
    }

    // ========== 事务 ==========

    @Test
    @DisplayName("事务提交")
    void testTransactionCommit() throws SQLException {
        TransactionManager.withTransaction(source, connection -> {
            dao.create(connection, new TestUser("Steve", 20, 100.0));
            dao.create(connection, new TestUser("Alex", 25, 200.0));
        });

        List<TestUser> all = dao.queryForAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("事务回滚")
    void testTransactionRollback() throws SQLException {
        try {
            TransactionManager.withTransaction(source, connection -> {
                dao.create(connection, new TestUser("Steve", 20, 100.0));
                throw new RuntimeException("simulated failure");
            });
        } catch (SQLException ignored) {
        }

        List<TestUser> all = dao.queryForAll();
        assertEquals(0, all.size(), "no row should remain after rollback");
    }

    @Test
    @DisplayName("事务回调返回值")
    void testTransactionCallable() throws SQLException {
        String result = TransactionManager.withTransactionResult(source, connection -> {
            dao.create(connection, new TestUser("Steve", 20, 100.0));
            return "done";
        });

        assertEquals("done", result);
        assertEquals(1, dao.queryForAll().size());
    }

    // ========== DaoManager ==========

    @Test
    @DisplayName("DaoManager 缓存")
    void testDaoManagerCache() throws SQLException {
        Dao<TestUser> dao1 = DaoManager.createDao(source, TestUser.class);
        Dao<TestUser> dao2 = DaoManager.createDao(source, TestUser.class);
        assertSame(dao1, dao2, "the same entity class should return the same DAO instance");
    }

    @Test
    @DisplayName("DaoManager createDaoNoCache 不缓存")
    void testDaoManagerNoCache() throws SQLException {
        Dao<TestUser> dao1 = DaoManager.createDaoNoCache(source, TestUser.class);
        Dao<TestUser> dao2 = DaoManager.createDaoNoCache(source, TestUser.class);
        assertNotSame(dao1, dao2, "createDaoNoCache should return a different instance");
    }

    // ========== TableUtils ==========

    @Test
    @DisplayName("dropTable 后建表")
    void testDropAndCreate() throws SQLException {
        TableUtils.dropTable(source, TestUser.class);
        TableUtils.createTableIfNotExists(source, TestUser.class);

        dao.create(new TestUser("Steve", 20, 100.0));
        assertEquals(1, dao.queryForAll().size());
    }

    @Test
    @DisplayName("clearTable 清空数据")
    void testClearTable() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));

        TableUtils.clearTable(source, TestUser.class);
        assertTrue(dao.queryForAll().isEmpty());
    }

    // ========== 方言 SQL ==========

    @Test
    @DisplayName("各方言 replace 语句的占位符数量与绑定列数量一致")
    void testReplaceSqlPlaceholdersMatchBoundColumns() {
        TableInfo tableInfo = TableInfo.of(TestUser.class);
        int boundColumns = tableInfo.getColumns().size();

        for (DatabaseDialect dialect : List.of(new H2Dialect(), new MysqlDialect(), new SqliteDialect(),
            new PostgresqlDialect())) {
            String sql = dialect.generateReplaceSql(tableInfo);
            String name = dialect.getClass().getSimpleName();
            assertEquals(boundColumns, countPlaceholders(sql),
                name + " replace placeholder count should equal the bound column count: " + sql);
            assertTrue(sql.contains(dialect.quoteIdentifier("id")),
                name + " replace statement must contain the primary key column, otherwise existing rows can never be matched: " + sql);
        }

        assertTrue(new H2Dialect().generateReplaceSql(tableInfo).contains("MERGE INTO"));
        assertTrue(new MysqlDialect().generateReplaceSql(tableInfo).contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(new SqliteDialect().generateReplaceSql(tableInfo).contains("INSERT OR REPLACE"));
        assertTrue(new PostgresqlDialect().generateReplaceSql(tableInfo).contains("ON CONFLICT (\"id\") DO UPDATE SET"));
    }

    @Test
    @DisplayName("MySQL 方言的 replace 语句能真正执行（H2 MySQL 兼容模式）")
    void testMysqlReplaceExecutes() throws SQLException {
        String url = "jdbc:h2:mem:test_replace_mysql;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        ConnectionSource h2Source = new JdbcConnectionSource(url);
        ConnectionSource mysqlLikeSource = new JdbcConnectionSource(url, null, null, new MysqlDialect());
        try {
            TableUtils.createTableIfNotExists(h2Source, TestUser.class);
            Dao<TestUser> h2Dao = DaoManager.createDaoNoCache(h2Source, TestUser.class);
            TestUser user = new TestUser("Steve", 20, 100.0);
            h2Dao.create(user);

            user.setBalance(999.0);
            Dao<TestUser> mysqlDao = DaoManager.createDaoNoCache(mysqlLikeSource, TestUser.class);
            mysqlDao.replace(user);

            TestUser found = h2Dao.queryForId(user.getId());
            assertEquals(999.0, found.getBalance(), 0.001, "replace should update the existing row");
            assertEquals(1, h2Dao.queryForAll().size(), "replace must not produce a duplicate row");
        } finally {
            h2Source.close();
            mysqlLikeSource.close();
        }
    }

    // ========== 列类型与长度 ==========

    @Test
    @DisplayName("ColumnType 与 length 生成对应的方言列定义")
    void testColumnTypeDdl() {
        TableInfo tableInfo = TableInfo.of(TypedUser.class);

        String mysql = new MysqlDialect().generateCreateTableSql(tableInfo);
        assertTrue(mysql.contains("`username` VARCHAR(255)"), mysql);
        assertTrue(mysql.contains("`bio` VARCHAR(1000)"), mysql);
        assertTrue(mysql.contains("`content` TEXT"), mysql);
        assertTrue(mysql.contains("`level` TINYINT"), mysql);
        assertTrue(mysql.contains("`flag` TINYINT(1)"), mysql);
        assertTrue(mysql.endsWith("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4"), mysql);

        String sqlite = new SqliteDialect().generateCreateTableSql(tableInfo);
        assertTrue(sqlite.contains("\"bio\" VARCHAR(1000)"), sqlite);
        assertTrue(sqlite.contains("\"content\" TEXT"), sqlite);
        assertTrue(sqlite.contains("\"id\" INTEGER PRIMARY KEY AUTOINCREMENT"), sqlite);

        String h2 = new H2Dialect().generateCreateTableSql(tableInfo);
        assertTrue(h2.contains("\"bio\" VARCHAR(1000)"), h2);
        assertTrue(h2.contains("\"content\" CLOB"), h2);
        assertTrue(h2.contains("\"id\" BIGINT AUTO_INCREMENT"), h2);

        String postgresql = new PostgresqlDialect().generateCreateTableSql(tableInfo);
        assertTrue(postgresql.contains("\"username\" VARCHAR(255)"), postgresql);
        assertTrue(postgresql.contains("\"bio\" VARCHAR(1000)"), postgresql);
        assertTrue(postgresql.contains("\"content\" TEXT"), postgresql);
        assertTrue(postgresql.contains("\"level\" SMALLINT"), postgresql);
        assertTrue(postgresql.contains("\"flag\" BOOLEAN"), postgresql);
        assertTrue(postgresql.contains("\"id\" BIGINT GENERATED BY DEFAULT AS IDENTITY"), postgresql);
    }

    @Test
    @DisplayName("非法列配置在解析阶段直接报错")
    void testInvalidColumnConfigRejected() {
        IllegalArgumentException negativeLength = assertThrows(IllegalArgumentException.class,
            () -> TableInfo.of(NegativeLengthEntity.class));
        assertTrue(negativeLength.getMessage().contains("length"), negativeLength.getMessage());

        IllegalArgumentException textWithLength = assertThrows(IllegalArgumentException.class,
            () -> TableInfo.of(TextWithLengthEntity.class));
        assertTrue(textWithLength.getMessage().contains("VARCHAR"), textWithLength.getMessage());
    }

    @Test
    @DisplayName("自定义长度与大文本可以正常写入读取")
    void testCustomLengthRoundTrip() throws SQLException {
        ConnectionSource textSource = TestDatabases.source("test_typed");
        try {
            TestDatabases.resetTable(textSource, TypedUser.class);
            Dao<TypedUser> typedDao = DaoManager.createDaoNoCache(textSource, TypedUser.class);

            String bio = repeat('a', 1000);
            String content = repeat('b', 5000);
            TypedUser user = new TypedUser();
            user.username = "Steve";
            user.bio = bio;
            user.content = content;
            user.level = 3;
            user.flag = true;
            typedDao.create(user);

            TypedUser found = typedDao.queryForId(user.id);
            assertEquals(bio, found.bio, "VARCHAR(1000) column should store 1000 characters");
            assertEquals(content, found.content, "TEXT column should store 5000 characters");
            assertEquals(3, found.level);
            assertTrue(found.flag);
        } finally {
            textSource.close();
        }
    }

    @Test
    @DisplayName("枚举按常量名最大长度映射，BigDecimal 映射为定点数")
    void testEnumAndDecimalTypeMapping() {
        TableInfo tableInfo = TableInfo.of(MappedUser.class);

        String mysql = new MysqlDialect().generateCreateTableSql(tableInfo);
        assertTrue(mysql.contains("`role` VARCHAR(9)"), mysql);
        assertTrue(mysql.contains("`amount` DECIMAL(65, 30)"), mysql);

        String h2 = new H2Dialect().generateCreateTableSql(tableInfo);
        assertTrue(h2.contains("\"role\" VARCHAR(9)"), h2);
        assertTrue(h2.contains("\"amount\" DECIMAL(65, 30)"), h2);

        String sqlite = new SqliteDialect().generateCreateTableSql(tableInfo);
        assertTrue(sqlite.contains("\"role\" VARCHAR(9)"), sqlite);
        assertTrue(sqlite.contains("\"amount\" DECIMAL(65, 30)"), sqlite);

        String postgresql = new PostgresqlDialect().generateCreateTableSql(tableInfo);
        assertTrue(postgresql.contains("\"role\" VARCHAR(9)"), postgresql);
        assertTrue(postgresql.contains("\"amount\" DECIMAL(65, 30)"), postgresql);
    }

    @Test
    @DisplayName("未支持的 Java 类型在生成建表语句时直接报错")
    void testUnsupportedJavaTypeRejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new H2Dialect().generateCreateTableSql(TableInfo.of(UnsupportedTypeEntity.class)));
        assertTrue(exception.getMessage().contains("createdAt"),
            "exception message should contain the field name: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("LocalDateTime"),
            "exception message should contain the Java type: " + exception.getMessage());
    }

    @Test
    @DisplayName("BigDecimal 与枚举可以正常写入读取")
    void testBigDecimalAndEnumRoundTrip() throws SQLException {
        ConnectionSource mappedSource = TestDatabases.source("test_mapped");
        try {
            TestDatabases.resetTable(mappedSource, MappedUser.class);
            Dao<MappedUser> mappedDao = DaoManager.createDaoNoCache(mappedSource, MappedUser.class);

            MappedUser user = new MappedUser();
            user.role = MappedRole.MODERATOR;
            // SQLite 的 DECIMAL(65, 30) 只有 NUMERIC 亲和性，超出 double 精度的值会按 REAL 存储而丢精度，
            // 因此该后端只用可被 double 精确表示的值验证往返
            user.amount = TestDatabases.isSqlite()
                ? new BigDecimal("12345.5")
                : new BigDecimal("12345.678901234567890123456789");
            mappedDao.create(user);

            MappedUser found = mappedDao.queryForId(user.id);
            assertEquals(MappedRole.MODERATOR, found.role);
            assertEquals(0, user.amount.compareTo(found.amount), "decimal value must not lose precision: " + found.amount);
        } finally {
            mappedSource.close();
        }
    }

    // ========== 元数据校验 ==========

    @Test
    @DisplayName("缺少主键的实体在解析时直接报错")
    void testEntityWithoutPrimaryKeyRejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> TableInfo.of(NoIdEntity.class));
        assertTrue(exception.getMessage().contains("NoIdEntity"),
            "exception message should contain the entity class name: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("primary key"), exception.getMessage());
    }

    // ========== DaoManager ==========

    @Test
    @DisplayName("DaoManager 缓存以连接源为键")
    void testDaoManagerCacheKeyedByConnectionSource() throws SQLException {
        ConnectionSource first = TestDatabases.source("test_cache_1");
        ConnectionSource second = TestDatabases.source("test_cache_2");
        try {
            Dao<CacheUser> firstDao = DaoManager.createDao(first, CacheUser.class);
            Dao<CacheUser> firstDaoAgain = DaoManager.createDao(first, CacheUser.class);
            Dao<CacheUser> secondDao = DaoManager.createDao(second, CacheUser.class);

            assertSame(firstDao, firstDaoAgain, "the same connection source should reuse the same DAO");
            assertNotSame(firstDao, secondDao, "different connection sources must not reuse the same DAO");
        } finally {
            first.close();
            second.close();
        }
    }

    // ========== 连接池与事务 ==========

    @Test
    @DisplayName("连接池下事务内使用回调连接可以回滚")
    void testTransactionRollbackWithPooledSource() throws SQLException {
        PooledConnectionSource pooledSource = TestDatabases.pooledSource("test_pool_tx");
        try {
            TestDatabases.resetTable(pooledSource, TestUser.class);
            Dao<TestUser> pooledDao = DaoManager.createDaoNoCache(pooledSource, TestUser.class);

            try {
                TransactionManager.withTransaction(pooledSource, connection -> {
                    pooledDao.create(connection, new TestUser("Steve", 20, 100.0));
                    pooledDao.create(connection, new TestUser("Alex", 25, 200.0));
                    throw new RuntimeException("simulated business failure");
                });
            } catch (SQLException ignored) {
            }
            assertEquals(0, pooledDao.queryForAll().size(), "rollback must also work with a pooled connection source");

            TransactionManager.withTransaction(pooledSource, connection -> {
                pooledDao.create(connection, new TestUser("Bob", 30, 300.0));
            });
            assertEquals(1, pooledDao.queryForAll().size(), "committed data should be visible");
        } finally {
            pooledSource.close();
        }
    }

    @Test
    @DisplayName("事务内写入只对事务连接可见")
    void testTransactionVisibilityWithPooledSource() throws SQLException {
        PooledConnectionSource pooledSource = TestDatabases.pooledSource("test_pool_visible");
        try {
            TestDatabases.resetTable(pooledSource, TestUser.class);
            Dao<TestUser> pooledDao = DaoManager.createDaoNoCache(pooledSource, TestUser.class);

            TransactionManager.withTransaction(pooledSource, connection -> {
                pooledDao.create(connection, new TestUser("Steve", 20, 100.0));
                assertEquals(1, pooledDao.queryForAll(connection).size(), "the transaction should see its own writes");

                // 不带 Connection 的方法在事务内会快速失败，这里显式再借一条连接来验证跨连接的可见性
                Connection otherConnection = pooledSource.getConnection();
                try {
                    assertEquals(0, pooledDao.queryForAll(otherConnection).size(),
                        "uncommitted writes must not be visible to other connections");
                } finally {
                    pooledSource.releaseConnection(otherConnection);
                }
            });

            assertEquals(1, pooledDao.queryForAll().size(), "data should be visible after commit");
        } finally {
            pooledSource.close();
        }
    }

    @Test
    @DisplayName("嵌套事务直接失败，且不影响后续事务")
    void testNestedTransactionRejected() {
        SQLException exception = assertThrows(SQLException.class, () ->
            TransactionManager.withTransaction(source, connection ->
                TransactionManager.withTransaction(source, innerConnection -> {
                })));
        assertNotNull(exception.getCause(), "the original exception of the nested transaction should be kept as the cause");
        assertTrue(exception.getCause() instanceof IllegalStateException, "cause=" + exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("Nested transactions"),
            exception.getCause().getMessage());

        assertDoesNotThrow(() -> TransactionManager.withTransaction(source, connection -> {
        }), "the in-transaction flag must not remain set after a failure");
    }

    @Test
    @DisplayName("并发借出连接不会超过 maxConnections")
    void testMaxConnectionsUnderConcurrency() throws Exception {
        PooledConnectionSource pooledSource = TestDatabases.pooledSource("test_pool_limit");
        pooledSource.setMaxConnections(2);
        ExecutorService executor = Executors.newFixedThreadPool(16);
        try {
            CountDownLatch start = new CountDownLatch(1);
            AtomicInteger maxSeen = new AtomicInteger();
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 16; i++) {
                futures.add(executor.submit(() -> {
                    start.await();
                    for (int k = 0; k < 20; k++) {
                        Connection connection = pooledSource.getConnection();
                        try {
                            maxSeen.accumulateAndGet(pooledSource.getTotalConnections(), Math::max);
                        } finally {
                            pooledSource.releaseConnection(connection);
                        }
                    }
                    return null;
                }));
            }
            start.countDown();
            for (Future<?> future : futures) {
                future.get();
            }

            assertTrue(maxSeen.get() <= 2, "the connection count must not exceed maxConnections under concurrency, actual peak=" + maxSeen.get());
            assertEquals(0, pooledSource.getActiveConnections(), "no active connection should remain after all connections are returned");
        } finally {
            executor.shutdownNow();
            pooledSource.close();
        }
    }

    @Test
    @DisplayName("空闲超时的连接会被心跳清理")
    void testIdleConnectionsEvicted() throws Exception {
        PooledConnectionSource pooledSource = TestDatabases.pooledSource("test_pool_idle");
        pooledSource.setCheckConnectionsEveryMs(50).setMaxIdleTimeMs(1);
        try {
            Connection connection = pooledSource.getConnection();
            pooledSource.releaseConnection(connection);
            assertEquals(1, pooledSource.getFreeConnections());

            long deadline = System.currentTimeMillis() + 5000;
            while (pooledSource.getFreeConnections() > 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }

            assertEquals(0, pooledSource.getFreeConnections(), "idle connections should be evicted by the heartbeat");
            assertEquals(0, pooledSource.getTotalConnections());
        } finally {
            pooledSource.close();
        }
    }

    // ========== 资源关闭 ==========
    @Test
    @DisplayName("DAO 操作结束后不残留未关闭的 Statement")
    void testStatementsClosed() throws SQLException {
        ConnectionSource realSource = TestDatabases.source("test_statement");
        CountingConnectionSource countingSource = new CountingConnectionSource(realSource);
        try {
            TestDatabases.resetTable(countingSource, TestUser.class);
            Dao<TestUser> countingDao = DaoManager.createDaoNoCache(countingSource, TestUser.class);

            TestUser user = new TestUser("Steve", 20, 100.0);
            countingDao.create(user);
            countingDao.queryForId(user.getId());
            countingDao.queryForAll();
            countingDao.queryBuilder().where(where -> where.greaterThan("age", 0)).query();
            countingDao.updateBuilder().set("balance", 5.0).where(where -> where.equals("id", user.getId())).update();
            countingDao.replace(user);
            countingDao.update(user);
            countingDao.delete(user);

            assertTrue(countingSource.getOpenedStatements() >= 8,
                "the probe should have observed statement creation, actual=" + countingSource.getOpenedStatements());
            assertEquals(0, countingSource.getOpenStatements(), "no unclosed PreparedStatement should remain");
        } finally {
            realSource.close();
        }
    }

    // ========== 非自增主键的插入 ==========

    @Test
    @DisplayName("业务主键（非自增）可以插入并查询")
    void testCreateWithNaturalPrimaryKey() throws SQLException {
        ConnectionSource naturalSource = TestDatabases.source("test_natural_key");
        try {
            TestDatabases.resetTable(naturalSource, NaturalKeyUser.class);
            Dao<NaturalKeyUser> naturalDao = DaoManager.createDaoNoCache(naturalSource, NaturalKeyUser.class);

            NaturalKeyUser user = new NaturalKeyUser();
            user.name = "alice";
            user.age = 20;
            assertEquals(1, naturalDao.create(user), "the primary key value must be written by the insert");

            NaturalKeyUser found = naturalDao.queryForId("alice");
            assertNotNull(found, "the row must be found by its business primary key");
            assertEquals(20, found.age);
        } finally {
            naturalSource.close();
        }
    }

    @Test
    @DisplayName("UUID 主键可以插入并查询")
    void testCreateWithUuidPrimaryKey() throws SQLException {
        ConnectionSource uuidSource = TestDatabases.source("test_uuid_key");
        try {
            TestDatabases.resetTable(uuidSource, UuidKeyUser.class);
            Dao<UuidKeyUser> uuidDao = DaoManager.createDaoNoCache(uuidSource, UuidKeyUser.class);

            UuidKeyUser user = new UuidKeyUser();
            user.id = UUID.randomUUID();
            user.name = "bob";
            assertEquals(1, uuidDao.create(user));

            UuidKeyUser found = uuidDao.queryForId(user.id);
            assertNotNull(found, "the row must be found by its UUID primary key");
            assertEquals(user.id, found.id);
            assertEquals("bob", found.name);
        } finally {
            uuidSource.close();
        }
    }

    @Test
    @DisplayName("INSERT 语句的列集合：非自增主键参与插入，自增主键被排除")
    void testInsertSqlColumnSet() {
        TableInfo naturalTable = TableInfo.of(NaturalKeyUser.class);
        String naturalSql = new H2Dialect().generateInsertSql(naturalTable);
        assertEquals(naturalTable.getColumns().size(), countPlaceholders(naturalSql),
            "a non-generated primary key must be part of the insert: " + naturalSql);
        assertTrue(naturalSql.contains("\"name\""), naturalSql);

        TableInfo generatedTable = TableInfo.of(TestUser.class);
        String generatedSql = new H2Dialect().generateInsertSql(generatedTable);
        assertEquals(generatedTable.getNonIdColumns().size(), countPlaceholders(generatedSql),
            "a generated primary key must not be part of the insert: " + generatedSql);
        assertFalse(generatedSql.contains("\"id\""), generatedSql);
    }

    @Test
    @DisplayName("自增主键为包装类型且未赋值时 replace 走插入")
    void testReplaceWithNullGeneratedId() throws SQLException {
        ConnectionSource wrapperSource = TestDatabases.source("test_wrapper_id");
        try {
            TestDatabases.resetTable(wrapperSource, WrapperIdUser.class);
            Dao<WrapperIdUser> wrapperDao = DaoManager.createDaoNoCache(wrapperSource, WrapperIdUser.class);

            WrapperIdUser user = new WrapperIdUser();
            user.name = "Steve";
            wrapperDao.replace(user);

            assertNotNull(user.id, "the generated id must be written back instead of binding a null key");
            assertNotNull(wrapperDao.queryForId(user.id));
        } finally {
            wrapperSource.close();
        }
    }

    // ========== 事务快速失败 ==========

    @Test
    @DisplayName("事务内使用不带 Connection 的方法会快速失败")
    void testConnectionLessCallInsideTransactionRejected() throws SQLException {
        SQLException exception = assertThrows(SQLException.class, () ->
            TransactionManager.withTransaction(source, connection -> dao.queryForAll()));
        assertTrue(exception.getCause() instanceof IllegalStateException, "cause=" + exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("escape the transaction"),
            exception.getCause().getMessage());

        assertTrue(dao.queryForAll().isEmpty(), "the failed transaction must be rolled back");
        assertDoesNotThrow(() -> TransactionManager.withTransaction(source, connection -> {
        }), "the in-transaction flag must not remain set after a failure");
    }

    // ========== 无条件更新 / 删除 / 空 SET ==========

    @Test
    @DisplayName("缺少 WHERE 的更新与删除直接失败")
    void testUnconditionalUpdateAndDeleteRejected() {
        assertThrows(IllegalStateException.class, () -> dao.updateBuilder().set("age", 1).buildSql());
        assertThrows(IllegalStateException.class, () -> dao.deleteBuilder().buildSql());

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> dao.deleteBuilder().delete());
        assertTrue(exception.getMessage().contains("delete every row"), exception.getMessage());
    }

    @Test
    @DisplayName("缺少 SET 列的更新直接失败")
    void testUpdateWithoutSetRejected() {
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> dao.updateBuilder().where(where -> where.equals("username", "Steve")).buildSql());
        assertTrue(exception.getMessage().contains("No column to update"), exception.getMessage());
    }

    // ========== 构建器边界 ==========

    @Test
    @DisplayName("未知列名在构建期直接失败")
    void testUnknownColumnRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> dao.queryBuilder().where(where -> where.equals("nickname", "Steve")));
        assertThrows(IllegalArgumentException.class, () -> dao.queryBuilder().orderBy("nickname", true));
        assertThrows(IllegalArgumentException.class, () -> dao.updateBuilder().set("nickname", "Steve"));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> dao.queryBuilder().where(where -> where.isNull("nickname")));
        assertTrue(exception.getMessage().contains("username"), "the message should list available columns: " + exception.getMessage());
    }

    @Test
    @DisplayName("悬空或重复的 AND/OR 直接失败")
    void testDanglingLogicalOperatorRejected() {
        IllegalStateException dangling = assertThrows(IllegalStateException.class,
            () -> dao.queryBuilder().where(where -> where.and()));
        assertTrue(dangling.getMessage().contains("must follow a condition"), dangling.getMessage());

        IllegalStateException duplicated = assertThrows(IllegalStateException.class,
            () -> dao.queryBuilder().where(where -> where.equals("username", "Steve").and().or()));
        assertTrue(duplicated.getMessage().contains("Duplicate"), duplicated.getMessage());
    }

    @Test
    @DisplayName("空 IN 与 null 条件值直接失败")
    void testEmptyInAndNullConditionRejected() {
        assertThrows(IllegalArgumentException.class,
            () -> dao.queryBuilder().where(where -> where.in("username")));
        assertThrows(IllegalArgumentException.class,
            () -> dao.queryBuilder().where(where -> where.in("username", "Steve", null)));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> dao.queryBuilder().where(where -> where.equals("username", null)));
        assertTrue(exception.getMessage().contains("isNull"), exception.getMessage());
    }

    @Test
    @DisplayName("limit(0) 生成 LIMIT 0，单独使用 offset 直接失败")
    void testLimitAndOffsetBoundaries() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        assertTrue(dao.queryBuilder().limit(0).buildSql().contains("LIMIT 0"),
            dao.queryBuilder().limit(0).buildSql());
        assertTrue(dao.queryBuilder().limit(0).query().isEmpty(), "limit(0) must really return no row");

        assertThrows(IllegalStateException.class, () -> dao.queryBuilder().offset(1).buildSql());
        assertThrows(IllegalArgumentException.class, () -> dao.queryBuilder().limit(-1));
        assertThrows(IllegalArgumentException.class, () -> dao.queryBuilder().offset(-1));
    }

    // ========== 类型转换与元数据 ==========

    @Test
    @DisplayName("条件值与 SET 值按列的 Java 类型转换")
    void testValueCoercedToColumnType() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));

        assertEquals(1, dao.queryBuilder().where(where -> where.equals("balance", 100)).query().size(),
            "an Integer condition value must be accepted by a double column");
        assertEquals(1, dao.queryBuilder().where(where -> where.in("age", 20L)).query().size(),
            "a Long condition value must be accepted by an int column");
        assertEquals(1, dao.updateBuilder().set("age", 21L)
            .where(where -> where.equals("username", "Steve")).update(),
            "a Long set value must be accepted by an int column");
        assertEquals(21, dao.queryForAll().get(0).getAge());
    }

    @Test
    @DisplayName("非 public 实体类可以实例化，并且可以把列更新为 NULL")
    void testNonPublicEntityAndNullValue() throws SQLException {
        ConnectionSource privateSource = TestDatabases.source("test_private");
        try {
            TestDatabases.resetTable(privateSource, PrivateUser.class);
            Dao<PrivateUser> privateDao = DaoManager.createDaoNoCache(privateSource, PrivateUser.class);

            PrivateUser user = new PrivateUser();
            user.nickname = "Steve";
            user.note = "hello";
            assertEquals(1, privateDao.create(user));

            PrivateUser found = privateDao.queryForId(user.id);
            assertEquals("Steve", found.nickname, "a non-public entity class must be instantiable");
            assertEquals("hello", found.note);

            assertEquals(1, privateDao.updateBuilder().set("note", null)
                .where(where -> where.equals("id", user.id)).update());
            assertNull(privateDao.queryForId(user.id).note, "the column should be updated to NULL");
        } finally {
            privateSource.close();
        }
    }

    @Test
    @DisplayName("标识符中的引号会被转义")
    void testIdentifierEscaping() {
        assertEquals("\"a\"\"b\"", new H2Dialect().quoteIdentifier("a\"b"));
        assertEquals("\"a\"\"b\"", new SqliteDialect().quoteIdentifier("a\"b"));
        assertEquals("`a``b`", new MysqlDialect().quoteIdentifier("a`b"));
    }

    // ========== 后端元数据 ==========

    @Test
    @DisplayName("建表结果与当前后端的元数据一致")
    void testBackendTableMetadata() throws SQLException {
        TestDatabases.resetTable(source, TypedUser.class);
        try (Connection connection = source.getConnection();
             Statement statement = connection.createStatement()) {
            System.out.println("[database-backend] " + TestDatabases.backend()
                + " version=" + queryOne(statement, TestDatabases.versionSql()));
            if (TestDatabases.isMysql()) {
                Map<String, String> columnTypes = new LinkedHashMap<>();
                try (ResultSet resultSet = statement.executeQuery(
                    "SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS"
                        + " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test_typed_users'")) {
                    while (resultSet.next()) {
                        columnTypes.put(resultSet.getString(1).toLowerCase(Locale.ROOT),
                            resultSet.getString(2).toLowerCase(Locale.ROOT));
                    }
                }
                assertEquals("varchar(255)", columnTypes.get("username"), columnTypes.toString());
                assertEquals("varchar(1000)", columnTypes.get("bio"), columnTypes.toString());
                assertEquals("text", columnTypes.get("content"), columnTypes.toString());
                assertEquals("tinyint(1)", columnTypes.get("flag"), columnTypes.toString());
                // MySQL 5.7 的 COLUMN_TYPE 会带上显示宽度（tinyint(4)），8.0.19 起不再输出，这里只校验类型本身
                assertTrue(columnTypes.get("level").startsWith("tinyint"), columnTypes.toString());
                assertTrue(queryOne(statement, mysqlColumnQuery("id", "EXTRA")).contains("auto_increment"),
                    "the primary key column must be auto increment on MySQL");

                String tableQuery = "SELECT %s FROM information_schema.TABLES"
                    + " WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'test_typed_users'";
                assertEquals("InnoDB", queryOne(statement, String.format(tableQuery, "ENGINE")),
                    "transactions silently stop working on MyISAM tables");
                String collation = queryOne(statement, String.format(tableQuery, "TABLE_COLLATION"));
                assertTrue(collation.startsWith("utf8mb4"), "expected an utf8mb4 collation, actual=" + collation);
            } else if (TestDatabases.isSqlite()) {
                Map<String, String> declaredTypes = new LinkedHashMap<>();
                try (ResultSet resultSet = statement.executeQuery("PRAGMA table_info('test_typed_users')")) {
                    while (resultSet.next()) {
                        declaredTypes.put(resultSet.getString("name"), resultSet.getString("type"));
                    }
                }
                assertEquals("VARCHAR(255)", declaredTypes.get("username"), declaredTypes.toString());
                assertEquals("VARCHAR(1000)", declaredTypes.get("bio"), declaredTypes.toString());
                assertEquals("TEXT", declaredTypes.get("content"), declaredTypes.toString());
                assertEquals("TINYINT", declaredTypes.get("level"), declaredTypes.toString());
                assertEquals("INTEGER", declaredTypes.get("flag"), declaredTypes.toString());
                String masterSql = queryOne(statement,
                    "SELECT sql FROM sqlite_master WHERE type = 'table' AND name = 'test_typed_users'");
                assertTrue(masterSql.contains("\"id\" INTEGER PRIMARY KEY AUTOINCREMENT"),
                    "SQLite requires the auto increment primary key to be declared inline: " + masterSql);
            } else if (TestDatabases.isPostgres()) {
                assertEquals("character varying", queryOne(statement, postgresColumnQuery("username", "data_type")));
                assertEquals("255", queryOne(statement, postgresColumnQuery("username", "character_maximum_length")));
                assertEquals("1000", queryOne(statement, postgresColumnQuery("bio", "character_maximum_length")));
                assertEquals("text", queryOne(statement, postgresColumnQuery("content", "data_type")));
                // PostgreSQL 没有 TINYINT，TINYINT 列落在 SMALLINT 上
                assertEquals("smallint", queryOne(statement, postgresColumnQuery("level", "data_type")));
                assertEquals("boolean", queryOne(statement, postgresColumnQuery("flag", "data_type")));
                assertEquals("YES", queryOne(statement, postgresColumnQuery("id", "is_identity")),
                    "the primary key column must be an identity column on PostgreSQL");
            } else {
                assertEquals("CHARACTER VARYING",
                    queryOne(statement, h2ColumnQuery("username", "DATA_TYPE")));
                assertEquals("255", queryOne(statement, h2ColumnQuery("username", "CHARACTER_MAXIMUM_LENGTH")));
                assertEquals("1000", queryOne(statement, h2ColumnQuery("bio", "CHARACTER_MAXIMUM_LENGTH")));
                assertEquals("TINYINT", queryOne(statement, h2ColumnQuery("level", "DATA_TYPE")));
                assertEquals("BOOLEAN", queryOne(statement, h2ColumnQuery("flag", "DATA_TYPE")));
                String contentType = queryOne(statement, h2ColumnQuery("content", "DATA_TYPE"));
                assertTrue(contentType.contains("LARGE OBJECT"), "H2 has no TEXT type, actual=" + contentType);
            }
        }
    }

    private static String mysqlColumnQuery(String column, String field) {
        return "SELECT " + field + " FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE()"
            + " AND TABLE_NAME = 'test_typed_users' AND COLUMN_NAME = '" + column + "'";
    }

    private static String h2ColumnQuery(String column, String field) {
        return "SELECT " + field + " FROM INFORMATION_SCHEMA.COLUMNS"
            + " WHERE TABLE_NAME = 'test_typed_users' AND COLUMN_NAME = '" + column + "'";
    }

    private static String postgresColumnQuery(String column, String field) {
        return "SELECT " + field + " FROM information_schema.columns WHERE table_schema = current_schema()"
            + " AND table_name = 'test_typed_users' AND column_name = '" + column + "'";
    }

    private static String queryOne(Statement statement, String sql) throws SQLException {
        try (ResultSet resultSet = statement.executeQuery(sql)) {
            assertTrue(resultSet.next(), "no row returned by: " + sql);
            return resultSet.getString(1);
        }
    }

    @Test
    @DisplayName("可以清除指定连接源的 DAO 缓存")
    void testClearCacheByConnectionSource() throws SQLException {
        ConnectionSource cacheSource = TestDatabases.source("test_clear_cache");
        try {
            Dao<CacheUser> cached = DaoManager.createDao(cacheSource, CacheUser.class);
            assertSame(cached, DaoManager.createDao(cacheSource, CacheUser.class));

            DaoManager.clearCache(cacheSource);
            assertNotSame(cached, DaoManager.createDao(cacheSource, CacheUser.class),
                "a cleared cache must build a new DAO");
        } finally {
            cacheSource.close();
        }
    }

    // ========== 测试用实体与辅助类 ==========

    @Table(name = "test_typed_users")
    public static class TypedUser {

        @Field(name = "id", id = true, generated = true)
        private long id;

        @Field(name = "username")
        private String username;

        @Field(name = "bio", length = 1000)
        private String bio;

        @Field(name = "content", type = Field.ColumnType.TEXT)
        private String content;

        @Field(name = "level", type = Field.ColumnType.TINYINT)
        private int level;

        @Field(name = "flag", type = Field.ColumnType.BOOLEAN)
        private boolean flag;

        public TypedUser() {
        }
    }

    public enum MappedRole {
        ADMIN, MODERATOR, USER
    }

    @Table(name = "test_mapped_users")
    public static class MappedUser {

        @Field(name = "id", id = true, generated = true)
        private long id;

        @Field(name = "role")
        private MappedRole role;

        @Field(name = "amount")
        private BigDecimal amount;

        public MappedUser() {
        }
    }

    @Table(name = "test_unsupported_type")
    private static class UnsupportedTypeEntity {

        @Field(name = "id", id = true, generated = true)
        private long id;

        @Field(name = "created_at")
        private LocalDateTime createdAt;
    }

    @Table(name = "test_bad_negative_length")
    private static class NegativeLengthEntity {

        @Field(name = "id", id = true, generated = true)
        private long id;

        @Field(name = "bio", length = -1)
        private String bio;
    }

    @Table(name = "test_bad_text_length")
    private static class TextWithLengthEntity {

        @Field(name = "id", id = true, generated = true)
        private long id;

        @Field(name = "content", type = Field.ColumnType.TEXT, length = 100)
        private String content;
    }

    private static String repeat(char ch, int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(ch);
        }
        return builder.toString();
    }

    @Table(name = "test_no_id")
    private static class NoIdEntity {
        @Field(name = "name")
        private String name;
    }

    @Table(name = "test_cache_users")
    private static class CacheUser {
        @Field(name = "id", id = true, generated = true)
        private long id;
        @Field(name = "name")
        private String name;
    }

    @Table(name = "test_natural_key_users")
    public static class NaturalKeyUser {

        @Field(name = "name", id = true)
        private String name;

        @Field(name = "age", defaultValue = "0")
        private int age;

        public NaturalKeyUser() {
        }
    }

    @Table(name = "test_uuid_key_users")
    public static class UuidKeyUser {

        @Field(name = "id", id = true)
        private UUID id;

        @Field(name = "name")
        private String name;

        public UuidKeyUser() {
        }
    }

    @Table(name = "test_wrapper_id_users")
    public static class WrapperIdUser {

        @Field(name = "id", id = true, generated = true)
        private Long id;

        @Field(name = "name")
        private String name;

        public WrapperIdUser() {
        }
    }

    @Table(name = "test_private_users")
    private static class PrivateUser {

        @Field(name = "id", id = true, generated = true)
        private long id;

        @Field(name = "nickname")
        private String nickname;

        @Field(name = "note")
        private String note;

        private PrivateUser() {
        }
    }

    /**
     * 统计 Statement 开关情况的连接源，用于验证 DAO 是否关闭语句
     */
    private static class CountingConnectionSource implements ConnectionSource {

        private final ConnectionSource delegate;
        private final AtomicInteger openStatements = new AtomicInteger();
        private final AtomicInteger openedStatements = new AtomicInteger();

        private CountingConnectionSource(ConnectionSource delegate) {
            this.delegate = delegate;
        }

        @Override
        public Connection getConnection() throws SQLException {
            Connection connection = delegate.getConnection();
            return (Connection) Proxy.newProxyInstance(
                CountingConnectionSource.class.getClassLoader(),
                new Class<?>[]{Connection.class},
                (proxy, method, args) -> {
                    Object result = invokeQuietly(method, connection, args);
                    if (result instanceof PreparedStatement) {
                        return wrapStatement((PreparedStatement) result);
                    }
                    return result;
                });
        }

        private PreparedStatement wrapStatement(PreparedStatement statement) {
            openStatements.incrementAndGet();
            openedStatements.incrementAndGet();
            return (PreparedStatement) Proxy.newProxyInstance(
                CountingConnectionSource.class.getClassLoader(),
                new Class<?>[]{PreparedStatement.class},
                (proxy, method, args) -> {
                    if ("close".equals(method.getName())) {
                        openStatements.decrementAndGet();
                    }
                    return invokeQuietly(method, statement, args);
                });
        }

        private static Object invokeQuietly(Method method, Object target, Object[] args) throws Throwable {
            try {
                return method.invoke(target, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        @Override
        public void releaseConnection(Connection connection) throws SQLException {
            delegate.releaseConnection(connection);
        }

        @Override
        public DatabaseDialect getDialect() {
            return delegate.getDialect();
        }

        @Override
        public boolean isOpen() {
            return delegate.isOpen();
        }

        @Override
        public void close() {
            delegate.close();
        }

        private int getOpenStatements() {
            return openStatements.get();
        }

        private int getOpenedStatements() {
            return openedStatements.get();
        }
    }

    private static int countPlaceholders(String sql) {
        int count = 0;
        for (int i = 0; i < sql.length(); i++) {
            if (sql.charAt(i) == '?') {
                count++;
            }
        }
        return count;
    }

}
