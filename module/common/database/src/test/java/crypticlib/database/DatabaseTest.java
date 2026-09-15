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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
        source = new JdbcConnectionSource("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        TableUtils.createTableIfNotExists(source, TestUser.class);
        dao = DaoManager.createDao(source, TestUser.class);
    }

    @AfterAll
    static void teardown() {
        source.close();
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
        assertTrue(user.getId() > 0, "自增 ID 应该被回填");

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
        assertEquals(originalId, found.getId(), "replace 应该保持原 ID");
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
                throw new RuntimeException("模拟异常");
            });
        } catch (SQLException ignored) {
        }

        List<TestUser> all = dao.queryForAll();
        assertEquals(0, all.size(), "事务回滚后应该没有数据");
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
        assertSame(dao1, dao2, "相同实体类应该返回同一个 DAO 实例");
    }

    @Test
    @DisplayName("DaoManager createDaoNoCache 不缓存")
    void testDaoManagerNoCache() throws SQLException {
        Dao<TestUser> dao1 = DaoManager.createDaoNoCache(source, TestUser.class);
        Dao<TestUser> dao2 = DaoManager.createDaoNoCache(source, TestUser.class);
        assertNotSame(dao1, dao2, "createDaoNoCache 应该返回不同实例");
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

        for (DatabaseDialect dialect : List.of(new H2Dialect(), new MysqlDialect(),
            new PostgresqlDialect(), new SqliteDialect())) {
            String sql = dialect.generateReplaceSql(tableInfo);
            String name = dialect.getClass().getSimpleName();
            assertEquals(boundColumns, countPlaceholders(sql),
                name + " 的 replace 占位符数量应等于绑定列数量: " + sql);
            assertTrue(sql.contains(dialect.quoteIdentifier("id")),
                name + " 的 replace 语句必须包含主键列，否则无法匹配已有记录: " + sql);
        }

        assertTrue(new H2Dialect().generateReplaceSql(tableInfo).contains("MERGE INTO"));
        assertTrue(new MysqlDialect().generateReplaceSql(tableInfo).contains("ON DUPLICATE KEY UPDATE"));
        assertTrue(new PostgresqlDialect().generateReplaceSql(tableInfo).contains("ON CONFLICT"));
        assertTrue(new SqliteDialect().generateReplaceSql(tableInfo).contains("INSERT OR REPLACE"));
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
            assertEquals(999.0, found.getBalance(), 0.001, "replace 应更新已有记录");
            assertEquals(1, h2Dao.queryForAll().size(), "replace 不应产生重复行");
        } finally {
            h2Source.close();
            mysqlLikeSource.close();
        }
    }

    // ========== 元数据校验 ==========
    @Test
    @DisplayName("缺少主键的实体在解析时直接报错")
    void testEntityWithoutPrimaryKeyRejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> TableInfo.of(NoIdEntity.class));
        assertTrue(exception.getMessage().contains("NoIdEntity"),
            "异常信息应包含实体类名: " + exception.getMessage());
        assertTrue(exception.getMessage().contains("主键"));
    }

    // ========== DaoManager ==========

    @Test
    @DisplayName("DaoManager 缓存以连接源为键")
    void testDaoManagerCacheKeyedByConnectionSource() throws SQLException {
        ConnectionSource first = new JdbcConnectionSource("jdbc:h2:mem:test_cache_1;DB_CLOSE_DELAY=-1");
        ConnectionSource second = new JdbcConnectionSource("jdbc:h2:mem:test_cache_2;DB_CLOSE_DELAY=-1");
        try {
            Dao<CacheUser> firstDao = DaoManager.createDao(first, CacheUser.class);
            Dao<CacheUser> firstDaoAgain = DaoManager.createDao(first, CacheUser.class);
            Dao<CacheUser> secondDao = DaoManager.createDao(second, CacheUser.class);

            assertSame(firstDao, firstDaoAgain, "同一连接源应复用同一个 DAO");
            assertNotSame(firstDao, secondDao, "不同连接源不能复用同一个 DAO");
        } finally {
            first.close();
            second.close();
        }
    }

    // ========== 连接池与事务 ==========

    @Test
    @DisplayName("连接池下事务内使用回调连接可以回滚")
    void testTransactionRollbackWithPooledSource() throws SQLException {
        PooledConnectionSource pooledSource = new PooledConnectionSource("jdbc:h2:mem:test_pool_tx;DB_CLOSE_DELAY=-1");
        try {
            TableUtils.createTableIfNotExists(pooledSource, TestUser.class);
            Dao<TestUser> pooledDao = DaoManager.createDaoNoCache(pooledSource, TestUser.class);

            try {
                TransactionManager.withTransaction(pooledSource, connection -> {
                    pooledDao.create(connection, new TestUser("Steve", 20, 100.0));
                    pooledDao.create(connection, new TestUser("Alex", 25, 200.0));
                    throw new RuntimeException("模拟业务失败");
                });
            } catch (SQLException ignored) {
            }
            assertEquals(0, pooledDao.queryForAll().size(), "连接池下回滚也必须生效");

            TransactionManager.withTransaction(pooledSource, connection -> {
                pooledDao.create(connection, new TestUser("Bob", 30, 300.0));
            });
            assertEquals(1, pooledDao.queryForAll().size(), "事务提交后应可见");
        } finally {
            pooledSource.close();
        }
    }

    @Test
    @DisplayName("事务内写入只对事务连接可见")
    void testTransactionVisibilityWithPooledSource() throws SQLException {
        PooledConnectionSource pooledSource = new PooledConnectionSource("jdbc:h2:mem:test_pool_visible;DB_CLOSE_DELAY=-1");
        try {
            TableUtils.createTableIfNotExists(pooledSource, TestUser.class);
            Dao<TestUser> pooledDao = DaoManager.createDaoNoCache(pooledSource, TestUser.class);

            TransactionManager.withTransaction(pooledSource, connection -> {
                pooledDao.create(connection, new TestUser("Steve", 20, 100.0));
                assertEquals(1, pooledDao.queryForAll(connection).size(), "事务内应能看到自己的写入");
                assertEquals(0, pooledDao.queryForAll().size(), "未提交时其它连接不应看到写入");
            });

            assertEquals(1, pooledDao.queryForAll().size(), "提交后应可见");
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
        assertNotNull(exception.getCause(), "嵌套事务的原始异常应保留在 cause 中");
        assertTrue(exception.getCause() instanceof IllegalStateException, "cause=" + exception.getCause());
        assertTrue(exception.getCause().getMessage().contains("嵌套"), exception.getCause().getMessage());

        assertDoesNotThrow(() -> TransactionManager.withTransaction(source, connection -> {
        }), "失败后不应残留事务进行中标记");
    }

    @Test
    @DisplayName("并发借出连接不会超过 maxConnections")
    void testMaxConnectionsUnderConcurrency() throws Exception {
        PooledConnectionSource pooledSource = new PooledConnectionSource("jdbc:h2:mem:test_pool_limit;DB_CLOSE_DELAY=-1");
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

            assertTrue(maxSeen.get() <= 2, "并发下连接总数峰值不应超过 maxConnections，实际峰值=" + maxSeen.get());
            assertEquals(0, pooledSource.getActiveConnections(), "全部归还后不应残留活跃连接");
        } finally {
            executor.shutdownNow();
            pooledSource.close();
        }
    }

    @Test
    @DisplayName("空闲超时的连接会被心跳清理")
    void testIdleConnectionsEvicted() throws Exception {
        PooledConnectionSource pooledSource = new PooledConnectionSource("jdbc:h2:mem:test_pool_idle;DB_CLOSE_DELAY=-1");
        pooledSource.setCheckConnectionsEveryMs(50).setMaxIdleTimeMs(1);
        try {
            Connection connection = pooledSource.getConnection();
            pooledSource.releaseConnection(connection);
            assertEquals(1, pooledSource.getFreeConnections());

            long deadline = System.currentTimeMillis() + 5000;
            while (pooledSource.getFreeConnections() > 0 && System.currentTimeMillis() < deadline) {
                Thread.sleep(50);
            }

            assertEquals(0, pooledSource.getFreeConnections(), "空闲超时的连接应被心跳清理");
            assertEquals(0, pooledSource.getTotalConnections());
        } finally {
            pooledSource.close();
        }
    }

    // ========== 资源关闭 ==========
    @Test
    @DisplayName("DAO 操作结束后不残留未关闭的 Statement")
    void testStatementsClosed() throws SQLException {
        ConnectionSource realSource = new JdbcConnectionSource("jdbc:h2:mem:test_statement;DB_CLOSE_DELAY=-1");
        CountingConnectionSource countingSource = new CountingConnectionSource(realSource);
        try {
            TableUtils.createTableIfNotExists(countingSource, TestUser.class);
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
                "探针应确实统计到语句创建，实际=" + countingSource.getOpenedStatements());
            assertEquals(0, countingSource.getOpenStatements(), "不应残留未关闭的 PreparedStatement");
        } finally {
            realSource.close();
        }
    }

    // ========== 测试用实体与辅助类 ==========

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
