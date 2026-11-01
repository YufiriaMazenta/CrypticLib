package crypticlib.scheduler;

import crypticlib.CrypticLib;
import crypticlib.CrypticLibPlugin;
import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifecyclePhase;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.LifecycleTaskConfig;
import crypticlib.lifecycle.LifecycleSchedule;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@LifecycleTaskConfig(
    schedules = @LifecycleSchedule(phase = LifecyclePhase.INIT),
    platforms = PlatformSide.BUNGEE
)
public enum BungeeScheduler implements Scheduler, LifecycleTask {

    INSTANCE;

    private Plugin plugin;
    private final ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(8, r -> {
        Thread t = new Thread(r, "CrypticLib-Async-Scheduler");
        t.setDaemon(true);
        return t;
    });
    private final Set<ScheduledFuture<?>> asyncFutures = ConcurrentHashMap.newKeySet();

    @Override
    public TaskWrapper sync(@NotNull Runnable task) {
        return new BungeeTaskWrapper(plugin.getProxy().getScheduler().schedule(plugin, task, 0, TimeUnit.MILLISECONDS));
    }

    @Override
    public TaskWrapper async(@NotNull Runnable task) {
        return new BungeeTaskWrapper(plugin.getProxy().getScheduler().runAsync(plugin, task));
    }

    @Override
    public TaskWrapper syncLater(@NotNull Runnable task, long delayTicks) {
        return new BungeeTaskWrapper(
            plugin.getProxy().getScheduler().schedule(plugin, task, delayTicks * 50, TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public TaskWrapper asyncLater(@NotNull Runnable task, long delayTicks) {
        if (delayTicks <= 0) {
            //零延迟没有可延迟的任务, 直接异步执行, 不进入待取消集合
            return async(task);
        }
        //一次性任务执行完毕后在下次调度时被裁掉, 避免 asyncFutures 只增不减造成内存泄漏
        pruneFinishedFutures();
        ScheduledFuture<?> future = asyncExecutor.schedule(safeTask(task, null), delayTicks * 50, TimeUnit.MILLISECONDS);
        asyncFutures.add(future);
        return new BungeeTaskWrapper(future);
    }

    @Override
    public TaskWrapper syncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BungeeTaskWrapper(
            plugin.getProxy().getScheduler().schedule(plugin, task, delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS)
        );
    }

    @Override
    public TaskWrapper asyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        pruneFinishedFutures();
        //holder 用于在任务体内拿到 future 以便异常时取消自己
        final ScheduledFuture<?>[] holder = new ScheduledFuture<?>[1];
        ScheduledFuture<?> future = asyncExecutor.scheduleAtFixedRate(
            safeTask(task, holder), delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS);
        holder[0] = future;
        asyncFutures.add(future);
        return new BungeeTaskWrapper(future);
    }

    /**
     * 包装异步任务: 捕获并记录异常。
     * <p>
     * {@link java.util.concurrent.ScheduledExecutorService} 在任务抛出异常后会静默抑制该任务的后续执行,
     * 异常只留在没人读取的 Future 里, 表现为"定时任务无声无息地消失"。
     * 因此这里自行捕获并记录日志, 定时任务还需取消自身, 避免变成每周期刷屏。
     *
     * @param task   实际任务
     * @param holder 定时任务的 future 持有者, 一次性任务传 null
     */
    @NotNull
    private Runnable safeTask(@NotNull Runnable task, ScheduledFuture<?>[] holder) {
        return () -> {
            try {
                task.run();
            } catch (Throwable throwable) {
                CrypticLib.error("Async scheduled task failed: " + task.getClass().getName(), throwable);
                ScheduledFuture<?> future = holder == null ? null : holder[0];
                if (future != null) {
                    future.cancel(false);
                    return;
                }
                //零延迟定时任务首次执行时 future 尚未回填, 只能把异常交回执行器
                if (throwable instanceof RuntimeException) {
                    throw (RuntimeException) throwable;
                }
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                }
                throw new RuntimeException(throwable);
            }
        };
    }

    /**
     * 清掉已经结束(完成或被取消)的一次性任务。
     * 相比"任务内部自我移除"，这里没有 zero-delay 下 future 尚未赋值就被执行、导致移除失效的竞态。
     */
    private void pruneFinishedFutures() {
        asyncFutures.removeIf(ScheduledFuture::isDone);
    }

    @Override
    public void cancelTasks() {
        plugin.getProxy().getScheduler().cancel(plugin);
        for (ScheduledFuture<?> future : asyncFutures) {
            future.cancel(false);
        }
        asyncFutures.clear();
    }

    @Override
    public void onLifecycle(CrypticLibPlugin plugin, LifecyclePhase lifecyclePhase) {
        this.plugin = (Plugin) plugin;
    }

}
