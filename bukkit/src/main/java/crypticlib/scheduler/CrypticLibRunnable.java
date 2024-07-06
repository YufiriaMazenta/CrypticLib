package crypticlib.scheduler;

import crypticlib.CrypticLibBukkit;
import crypticlib.scheduler.task.ITaskWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public abstract class CrypticLibRunnable implements Runnable {

    protected ITaskWrapper taskWrapper;

    @Override
    public abstract void run();

    public ITaskWrapper runTask(Plugin plugin) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTask(plugin, this));
    }

    public ITaskWrapper runTaskLater(Plugin plugin, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskLater(plugin, this, delayTicks));
    }

    public ITaskWrapper runTaskTimer(Plugin plugin, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskTimer(plugin, this, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskAsync(Plugin plugin) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskAsync(plugin, this));
    }

    public ITaskWrapper runTaskLaterAsync(Plugin plugin, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskLaterAsync(plugin, this, delayTicks));
    }

    public ITaskWrapper runTaskTimerAsync(Plugin plugin, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskTimerAsync(plugin, this, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskOnLocation(Plugin plugin, Location location) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskOnLocation(plugin, location, this));
    }

    public ITaskWrapper runTaskOnLocationLater(Plugin plugin, Location location, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskOnLocationLater(plugin, location, this, delayTicks));
    }

    public ITaskWrapper runTaskOnLocationTimer(Plugin plugin, Location location, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskOnLocationTimer(plugin, location, this, delayTicks, periodTicks));
    }

    public ITaskWrapper runTaskOnEntity(Plugin plugin, Entity entity) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskOnEntity(plugin, entity, this, this));
    }

    public ITaskWrapper runTaskOnEntityLater(Plugin plugin, Entity entity, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskOnEntityLater(plugin, entity, this, this, delayTicks));
    }

    public ITaskWrapper runTaskOnEntityTimer(Plugin plugin, Entity entity, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runTaskOnEntityTimer(plugin, entity, this, this, delayTicks, periodTicks));
    }

    public void cancel() {
        checkTaskNull();
        this.taskWrapper.cancel();
    }

    public boolean isCancelled() {
        checkTaskNull();
        return this.taskWrapper.isCancelled();
    }

    protected ITaskWrapper setTaskWrapper(ITaskWrapper taskWrapper) {
        this.taskWrapper = taskWrapper;
        return this.taskWrapper;
    }

    protected void checkTaskNotNull() {
        if (this.taskWrapper != null) {
            throw new IllegalArgumentException("Runnable is null");
        }
    }

    protected void checkTaskNull() {
        if (this.taskWrapper == null) {
            throw new IllegalArgumentException("Task is null");
        }
    }
    
}
