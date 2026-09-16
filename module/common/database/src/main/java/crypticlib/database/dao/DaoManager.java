package crypticlib.database.dao;

import crypticlib.database.connection.ConnectionSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DAO 管理器，负责创建和缓存 DAO 实例
 * <p>
 * 缓存以「连接源 + 实体类」为键：同一个实体类在不同连接源上会得到各自的 DAO，
 * 避免更换或重建连接源后仍拿到绑定旧连接源的 DAO。
 */
public class DaoManager {

    private static final Map<ConnectionSource, Map<Class<?>, Dao<?>>> CACHE = new ConcurrentHashMap<>();

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
        return (Dao<T>) CACHE.computeIfAbsent(connectionSource, source -> new ConcurrentHashMap<>())
            .computeIfAbsent(entityClass, clazz -> new BaseDao<>(connectionSource, entityClass));
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
     * <p>
     * 缓存是静态强引用，会在连接源还存活期间持有它以及它上面创建过的实体类；
     * 只清指定连接源的缓存请用 {@link #clearCache(ConnectionSource)}
     */
    public static void clearCache() {
        CACHE.clear();
    }

    /**
     * 清除指定连接源的缓存
     * <p>
     * 宿主在关闭/重建连接源时（例如插件卸载、数据库切换）应调用本方法，
     * 否则该连接源与其加载的实体类不会被回收，反复创建连接源会造成类加载器泄漏
     *
     * @param connectionSource 要清除缓存的连接源
     */
    public static void clearCache(ConnectionSource connectionSource) {
        if (connectionSource == null) {
            return;
        }
        CACHE.remove(connectionSource);
    }

    /**
     * 获取缓存的 DAO 数量
     */
    public static int getCacheSize() {
        int size = 0;
        for (Map<Class<?>, Dao<?>> daoMap : CACHE.values()) {
            size += daoMap.size();
        }
        return size;
    }

}
