package crypticlib.database.dao;

import crypticlib.database.query.DeleteBuilder;
import crypticlib.database.query.QueryBuilder;
import crypticlib.database.query.UpdateBuilder;

import java.sql.SQLException;
import java.util.List;

/**
 * 数据访问对象接口
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
     * 查询所有记录
     */
    List<T> queryForAll() throws SQLException;

    /**
     * 使用 QueryBuilder 查询
     */
    List<T> query(QueryBuilder<T> queryBuilder) throws SQLException;

    /**
     * 使用 UpdateBuilder 条件更新
     *
     * @return 影响的行数
     */
    int update(UpdateBuilder<T> updateBuilder) throws SQLException;

    /**
     * 使用 DeleteBuilder 条件删除
     *
     * @return 影响的行数
     */
    int delete(DeleteBuilder<T> deleteBuilder) throws SQLException;

    /**
     * 插入一条记录
     *
     * @return 影响的行数
     */
    int create(T entity) throws SQLException;

    /**
     * 根据主键更新一条记录
     *
     * @return 影响的行数
     */
    int update(T entity) throws SQLException;

    /**
     * 根据主键删除一条记录
     *
     * @return 影响的行数
     */
    int delete(T entity) throws SQLException;

    /**
     * 插入或替换一条记录（INSERT OR REPLACE）
     * <p>
     * - SQLite: INSERT OR REPLACE
     * - MySQL: INSERT ... ON DUPLICATE KEY UPDATE
     * - PostgreSQL: INSERT ... ON CONFLICT DO UPDATE
     * - H2: MERGE INTO
     *
     * @return 影响的行数
     */
    int replace(T entity) throws SQLException;

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
