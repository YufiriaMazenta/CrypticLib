package crypticlib.scheduler;

import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.LifeCycleTask;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.TaskRule;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@LifeCycleTaskSettings(
    rules = @TaskRule(lifeCycle = LifeCycle.INIT),
    platforms = PlatformSide.BUNGEE
)
public enum BungeeScheduler implements Scheduler, LifeCycleTask {

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
        //一次性任务执行完毕后从集合中移除自身,避免 asyncFutures 只增不减造成内存泄漏
        ScheduledFuture<?>[] futureHolder = new ScheduledFuture<?>[1];
        ScheduledFuture<?> future = asyncExecutor.schedule(() -> {
            try {
                task.run();
            } finally {
                asyncFutures.remove(futureHolder[0]);
            }
        }, delayTicks * 50, TimeUnit.MILLISECONDS);
        futureHolder[0] = future;
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
        ScheduledFuture<?> future = asyncExecutor.scheduleAtFixedRate(task, delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS);
        asyncFutures.add(future);
        return new BungeeTaskWrapper(future);
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
    public void lifecycle(Object plugin, LifeCycle lifeCycle) {
        this.plugin = (Plugin) plugin;
    }

}
