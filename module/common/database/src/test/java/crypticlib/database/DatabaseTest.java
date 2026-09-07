package crypticlib.database;

import crypticlib.database.connection.ConnectionSource;
import crypticlib.database.connection.JdbcConnectionSource;
import crypticlib.database.dao.Dao;
import crypticlib.database.dao.DaoManager;
import crypticlib.database.table.TableUtils;
import crypticlib.database.transaction.TransactionManager;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

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
        TransactionManager.withTransaction(source, () -> {
            dao.create(new TestUser("Steve", 20, 100.0));
            dao.create(new TestUser("Alex", 25, 200.0));
        });

        List<TestUser> all = dao.queryForAll();
        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("事务回滚")
    void testTransactionRollback() throws SQLException {
        try {
            TransactionManager.withTransaction(source, () -> {
                dao.create(new TestUser("Steve", 20, 100.0));
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
        String result = TransactionManager.withTransaction(source, () -> {
            dao.create(new TestUser("Steve", 20, 100.0));
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

}
