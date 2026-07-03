package crypticlib.scheduler;

import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.LifeCycleTask;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.TaskRule;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

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
    private final ScheduledExecutorService asyncExecutor = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "CrypticLib-Async-Scheduler");
        t.setDaemon(true);
        return t;
    });

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
        ScheduledFuture<?> future = asyncExecutor.schedule(task, delayTicks * 50, TimeUnit.MILLISECONDS);
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
        return new BungeeTaskWrapper(future);
    }

    @Override
    public void cancelTasks() {
        plugin.getProxy().getScheduler().cancel(plugin);
        asyncExecutor.shutdownNow();
    }

    @Override
    public void lifecycle(Object plugin, LifeCycle lifeCycle) {
        this.plugin = (Plugin) plugin;
    }

}
