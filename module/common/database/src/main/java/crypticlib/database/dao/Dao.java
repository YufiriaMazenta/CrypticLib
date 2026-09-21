package crypticlib.database.dao;

import crypticlib.database.statement.DeleteBuilder;
import crypticlib.database.statement.QueryBuilder;
import crypticlib.database.statement.UpdateBuilder;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * 数据访问对象接口
 * <p>
 * 每个方法都有两个版本：
 * <ul>
 *     <li>不接收 Connection 的版本会自行向 ConnectionSource 借用连接并归还，适合事务外使用。</li>
 *     <li>接收 Connection 的版本使用调用方传入的连接，事务中必须使用该版本，才能让操作落在同一条事务连接上。</li>
 * </ul>
 * 在事务中调用不接收 Connection 的版本会直接抛 IllegalStateException，而不是静默地在另一条连接上执行。
 *
 * @param <T> 实体类型
 */
public interface Dao<T> {

    /**
     * 根据 ID 查询
     *
     * @param id 主键值
     * @return 实体对象，不存在返回 null
     */
    T queryForId(Object id) throws SQLException;

    /**
     * 根据 ID 查询（使用指定连接）
     */
    T queryForId(Connection connection, Object id) throws SQLException;

    /**
     * 查询所有记录
     */
    List<T> queryForAll() throws SQLException;

    /**
     * 查询所有记录（使用指定连接）
     */
    List<T> queryForAll(Connection connection) throws SQLException;

    /**
     * 使用 QueryBuilder 查询
     */
    List<T> query(QueryBuilder<T> queryBuilder) throws SQLException;

    /**
     * 使用 QueryBuilder 查询（使用指定连接）
     */
    List<T> query(Connection connection, QueryBuilder<T> queryBuilder) throws SQLException;

    /**
     * 使用 UpdateBuilder 条件更新
     *
     * @return 影响的行数
     */
    int update(UpdateBuilder<T> updateBuilder) throws SQLException;

    /**
     * 使用 UpdateBuilder 条件更新（使用指定连接）
     *
     * @return 影响的行数
     */
    int update(Connection connection, UpdateBuilder<T> updateBuilder) throws SQLException;

    /**
     * 使用 DeleteBuilder 条件删除
     *
     * @return 影响的行数
     */
    int delete(DeleteBuilder<T> deleteBuilder) throws SQLException;

    /**
     * 使用 DeleteBuilder 条件删除（使用指定连接）
     *
     * @return 影响的行数
     */
    int delete(Connection connection, DeleteBuilder<T> deleteBuilder) throws SQLException;

    /**
     * 插入一条记录
     * <p>
     * 自增主键由数据库生成并回写实体的主键字段；非自增主键（业务主键、UUID 等）会把实体里的主键值一并写入。
     *
     * @return 影响的行数
     */
    int create(T entity) throws SQLException;

    /**
     * 插入一条记录（使用指定连接）
     *
     * @return 影响的行数
     */
    int create(Connection connection, T entity) throws SQLException;

    /**
     * 根据主键更新一条记录
     *
     * @return 影响的行数
     */
    int update(T entity) throws SQLException;

    /**
     * 根据主键更新一条记录（使用指定连接）
     *
     * @return 影响的行数
     */
    int update(Connection connection, T entity) throws SQLException;

    /**
     * 根据主键删除一条记录
     *
     * @return 影响的行数
     */
    int delete(T entity) throws SQLException;

    /**
     * 根据主键删除一条记录（使用指定连接）
     *
     * @return 影响的行数
     */
    int delete(Connection connection, T entity) throws SQLException;

    /**
     * 插入或替换一条记录（INSERT OR REPLACE）
     * <p>
     * - SQLite: INSERT OR REPLACE
     * - MySQL: INSERT ... ON DUPLICATE KEY UPDATE
     * - H2: MERGE INTO
     * - PostgreSQL: INSERT ... ON CONFLICT (主键) DO UPDATE
     * <p>
     * 注意：各方言对「影响的行数」定义不一致，不要用 == 1 判断是否成功。
     * MySQL 的 INSERT ... ON DUPLICATE KEY UPDATE 在更新已有行时返回 2（插入返回 1，已有行且值未变化返回 0）；
     * SQLite 的 INSERT OR REPLACE 命中已有行时返回 1；
     * PostgreSQL 的 ON CONFLICT DO UPDATE 插入与更新都返回 1，只有主键列时退化为 DO NOTHING，命中冲突返回 0。
     * <p>
     * 自增主键没有值（null 或 0）时无从匹配已有行，会退化为普通 INSERT 由数据库生成主键，各方言行为一致。
     * 自增主键有值时会把该值一并写入；但 PostgreSQL 下显式写入主键不会推进 identity 序列，
     * 若写入的主键不小于序列的当前值，之后依赖数据库生成主键的 {@code create(...)} 会在序列发到该值时
     * 抛出主键冲突，越过该值后又恢复正常。需要向 PostgreSQL 写入靠后的显式主键时，
     * 请在写入后用 {@code setval} 把序列对齐到表中的最大主键值。
     *
     * @return 影响的行数
     */
    int replace(T entity) throws SQLException;

    /**
     * 插入或替换一条记录（使用指定连接），返回行数语义同 {@code replace(T entity)}
     *
     * @return 影响的行数
     */
    int replace(Connection connection, T entity) throws SQLException;

    /**
     * 创建 QueryBuilder
     */
    QueryBuilder<T> queryBuilder();

    /**
     * 创建 UpdateBuilder
     */
    UpdateBuilder<T> updateBuilder();

    /**
     * 创建 DeleteBuilder
     */
    DeleteBuilder<T> deleteBuilder();

    /**
     * 获取实体类
     */
    Class<T> getEntityClass();

}
