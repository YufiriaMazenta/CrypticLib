package crypticlib.scheduler;

import org.jetbrains.annotations.NotNull;

/**
 * 通用调度器接口。
 * <p>
 * <b>注意各平台 {@code sync} 系列方法的真实语义并不一致, 不要假设 "sync 任务彼此单线程串行":</b>
 * <ul>
 *     <li>Bukkit(Spigot): 映射到服务器主线程, 所有 sync 任务串行执行。</li>
 *     <li>Folia: 映射到全局区域(GlobalRegion)调度线程, 该线程<b>不能</b>安全操作具体实体/区块,
 *         对实体/世界的操作会抛线程检查异常; 需要操作具体实体/坐标时应使用
 *         {@code BukkitScheduler} 的 {@code runOnEntity*}/{@code runOnLocation*}。</li>
 *     <li>Bungee/Velocity: 走代理调度器的线程池, 两个 sync 任务可能在<b>不同线程并发</b>执行,
 *         没有任何互斥或顺序保证。依赖 "sync 串行" 保护共享状态的代码在代理端会产生数据竞争,
 *         应自行加锁。</li>
 * </ul>
 */
public interface Scheduler {

    /**
     * 提交一个 "同步" 任务。各平台线程语义不同, 详见 {@link Scheduler} 类注释,
     * 不要依赖跨平台的单线程串行假设。
     */
    TaskWrapper sync(@NotNull Runnable task);

    TaskWrapper async(@NotNull Runnable task);

    /**
     * 延迟提交一个 "同步" 任务。sync 语义详见 {@link Scheduler} 类注释。
     */
    TaskWrapper syncLater(@NotNull Runnable task, long delayTicks);

    TaskWrapper asyncLater(@NotNull Runnable task, long delayTicks);

    /**
     * 提交一个 "同步" 定时任务。sync 语义详见 {@link Scheduler} 类注释;
     * 在 Bungee/Velocity 上定时回调之间无串行保证。
     */
    TaskWrapper syncTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    TaskWrapper asyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    /**
     * 取消本调度器创建的任务。
     * <p>
     * 各平台覆盖范围不同: 在 Bukkit(Spigot) 上取消该插件的全部任务; 在 Folia 上,
     * 除全局区域/异步任务外, 通过 {@code runOnLocation*}/{@code runOnEntity*} 创建的
     * <b>定时</b>任务也会被取消, 但一次性的 region/entity 任务不在覆盖范围内
     * (Folia 的 RegionScheduler/EntityScheduler 不提供按插件取消的 API)。
     */
    void cancelTasks();

}
