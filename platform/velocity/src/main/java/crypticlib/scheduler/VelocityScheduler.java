package crypticlib.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import crypticlib.CrypticLibPlugin;
import crypticlib.PlatformSide;
import crypticlib.VelocityPlugin;
import crypticlib.lifecycle.Lifecycle;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.LifecycleTaskSettings;
import crypticlib.lifecycle.LifecycleRule;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

@LifecycleTaskSettings(
    rules = @LifecycleRule(lifeCycle = Lifecycle.INIT),
    platforms = PlatformSide.VELOCITY
)
public enum VelocityScheduler implements Scheduler, LifecycleTask {

    INSTANCE;

    private VelocityPlugin plugin;

    // Velocity没有主线程概念，所有任务都在代理事件循环上执行，sync和async行为一致
    @Override
    public TaskWrapper sync(@NotNull Runnable task) {
        return new VelocityTaskWrapper(
            plugin.proxyServer().getScheduler().buildTask(plugin, task).schedule()
        );
    }

    @Override
    public TaskWrapper async(@NotNull Runnable task) {
        return new VelocityTaskWrapper(
            plugin.proxyServer().getScheduler().buildTask(plugin, task).schedule()
        );
    }

    @Override
    public TaskWrapper syncLater(@NotNull Runnable task, long delayTicks) {
        return new VelocityTaskWrapper(
            plugin.proxyServer().getScheduler().buildTask(plugin, task)
                .delay(Duration.ofMillis(delayTicks * 50))
                .schedule()
        );
    }

    @Override
    public TaskWrapper asyncLater(@NotNull Runnable task, long delayTicks) {
        return syncLater(task, delayTicks);
    }

    @Override
    public TaskWrapper syncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new VelocityTaskWrapper(
            plugin.proxyServer().getScheduler().buildTask(plugin, task)
                .delay(Duration.ofMillis(delayTicks * 50))
                .repeat(Duration.ofMillis(periodTicks * 50))
                .schedule()
        );
    }

    @Override
    public TaskWrapper asyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return syncTimer(task, delayTicks, periodTicks);
    }

    @Override
    public void cancelTasks() {
        plugin.proxyServer().getScheduler().tasksByPlugin(plugin).forEach(ScheduledTask::cancel);
    }

    @Override
    public void lifecycle(CrypticLibPlugin plugin, Lifecycle lifeCycle) {
        this.plugin = (VelocityPlugin) plugin;
    }

}
