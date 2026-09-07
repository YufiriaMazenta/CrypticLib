package crypticlib.database.dao;

import crypticlib.database.connection.ConnectionSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO 管理器，负责创建和缓存 DAO 实例
 */
public class DaoManager {

    private static final Map<Class<?>, Dao<?>> CACHE = new ConcurrentHashMap<>();

    private DaoManager() {
    }

    /**
     * 创建或获取 DAO 实例（带缓存）
     *
     * @param connectionSource 连接源
     * @param entityClass      实体类
     * @param <T>              实体类型
     * @return DAO 实例
     */
    @SuppressWarnings("unchecked")
    public static <T> Dao<T> createDao(ConnectionSource connectionSource, Class<T> entityClass) {
        return (Dao<T>) CACHE.computeIfAbsent(entityClass, clazz -> new BaseDao<>(connectionSource, entityClass));
    }

    /**
     * 创建 DAO 实例（不缓存）
     *
     * @param connectionSource 连接源
     * @param entityClass      实体类
     * @param <T>              实体类型
     * @return DAO 实例
     */
    public static <T> Dao<T> createDaoNoCache(ConnectionSource connectionSource, Class<T> entityClass) {
        return new BaseDao<>(connectionSource, entityClass);
    }

    /**
     * 清除缓存
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * 获取缓存的 DAO 数量
     */
    public static int getCacheSize() {
        return CACHE.size();
    }

}
