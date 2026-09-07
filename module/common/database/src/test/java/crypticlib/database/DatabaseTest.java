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
    @DisplayName("查询所有")
    void testQueryForAll() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 300.0));

        List<TestUser> users = dao.queryForAll();
        assertEquals(3, users.size());
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
    }

    @Test
    @DisplayName("QueryBuilder 条件查询")
    void testQueryBuilder() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 50.0));

        // 等于
        List<TestUser> results = dao.queryBuilder()
            .where().equals("username", "Steve").done()
            .query();
        assertEquals(1, results.size());
        assertEquals("Steve", results.get(0).getUsername());

        // 大于
        results = dao.queryBuilder()
            .where().greaterThan("age", 20).done()
            .query();
        assertEquals(2, results.size());
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Alex")));
        assertTrue(results.stream().anyMatch(u -> u.getUsername().equals("Bob")));

        // 多条件 AND
        results = dao.queryBuilder()
            .where()
            .greaterThanOrEquals("age", 25)
            .and()
            .lessThan("balance", 200.0)
            .done()
            .query();
        assertEquals(1, results.size());
        assertEquals("Bob", results.get(0).getUsername());

        // 排序 + 限制
        results = dao.queryBuilder()
            .where().greaterThan("balance", 0.0).done()
            .orderBy("balance", false)
            .limit(2)
            .query();
        assertEquals(2, results.size());
        assertEquals("Alex", results.get(0).getUsername());
        assertEquals("Steve", results.get(1).getUsername());
    }

    @Test
    @DisplayName("UpdateBuilder 条件更新")
    void testUpdateBuilder() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 300.0));

        int updated = dao.updateBuilder()
            .set("balance", 0.0)
            .where().greaterThan("balance", 150.0).done()
            .execute();

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
    @DisplayName("DeleteBuilder 条件删除")
    void testDeleteBuilder() throws SQLException {
        dao.create(new TestUser("Steve", 20, 100.0));
        dao.create(new TestUser("Alex", 25, 200.0));
        dao.create(new TestUser("Bob", 30, 300.0));

        int deleted = dao.deleteBuilder()
            .where().lessThan("balance", 200.0).done()
            .execute();

        assertEquals(1, deleted);

        List<TestUser> remaining = dao.queryForAll();
        assertEquals(2, remaining.size());
    }

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

}
