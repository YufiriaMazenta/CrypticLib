package crypticlib.scheduler;

import crypticlib.CrypticLib;
import crypticlib.scheduler.task.ITaskWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class CrypticLibRunnable {

    protected ITaskWrapper taskWrapper;
    protected Runnable runnable;

    public CrypticLibRunnable(@NotNull Runnable runnable) {
        this.runnable = runnable;
    }

    public ITaskWrapper runTask(Plugin plugin) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTask(plugin, runnable));
    }

    public ITaskWrapper runTaskLater(Plugin plugin, long delayTicks) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskLater(plugin, runnable, delayTicks));
    }

    public ITaskWrapper runTaskTimer(Plugin plugin, long delayTicks, long periodTicks) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskTimer(plugin, runnable, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskAsync(Plugin plugin) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskAsync(plugin, runnable));
    }

    public ITaskWrapper runTaskLaterAsync(Plugin plugin, long delayTicks) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskLaterAsync(plugin, runnable, delayTicks));
    }

    public ITaskWrapper runTaskTimerAsync(Plugin plugin, long delayTicks, long periodTicks) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskTimerAsync(plugin, runnable, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskOnLocation(Plugin plugin, Location location) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskOnLocation(plugin, location, runnable));
    }

    public ITaskWrapper runTaskOnLocationLater(Plugin plugin, Location location, long delayTicks) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskOnLocationLater(plugin, location, runnable, delayTicks));
    }

    public ITaskWrapper runTaskOnLocationTimer(Plugin plugin, Location location, long delayTicks, long periodTicks) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskOnLocationTimer(plugin, location, runnable, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskOnEntity(Plugin plugin, Entity entity) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskOnEntity(plugin, entity, runnable, runnable));
    }

    public ITaskWrapper runTaskOnEntityLater(Plugin plugin, Entity entity, long delayTicks) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskOnEntityLater(plugin, entity, runnable, runnable, delayTicks));
    }

    public ITaskWrapper runTaskOnEntityTimer(Plugin plugin, Entity entity, long delayTicks, long periodTicks) {
        checkRunnable();
        return setTaskWrapper(CrypticLib.platform().scheduler().runTaskOnEntityTimer(plugin, entity, runnable, runnable, delayTicks, periodTicks));
    }

    public void cancel() {
        checkTask();
        this.taskWrapper.cancel();
    }

    public boolean isCancelled() {
        checkTask();
        return this.taskWrapper.isCancelled();
    }

    public Runnable runnable() {
        return runnable;
    }

    public CrypticLibRunnable setRunnable(Runnable runnable) {
        this.runnable = runnable;
        return this;
    }

    protected ITaskWrapper setTaskWrapper(ITaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
        return this.taskWrapper;
    }

    protected void checkRunnable() {
        if (this.runnable == null) {
            throw new IllegalArgumentException("Runnable is null");
        }
    }

    protected void checkTask() {
        if (this.taskWrapper == null) {
            throw new IllegalArgumentException("Task is null");
        }
    }
    
}
