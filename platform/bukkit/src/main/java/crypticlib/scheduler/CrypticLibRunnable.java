package crypticlib.scheduler;

import crypticlib.CrypticLibBukkit;
import crypticlib.scheduler.task.TaskWrapper;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;

public abstract class CrypticLibRunnable implements Runnable {

    protected TaskWrapper taskWrapper;

    @Override
    public abstract void run();

    public TaskWrapper sync() {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().sync(this));
    }

    public TaskWrapper syncLater(long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().syncLater(this, delayTicks));
    }

    public TaskWrapper syncTimer(long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().syncTimer(this, delayTicks, periodTicks));
    }

    public TaskWrapper async() {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().async(this));
    }

    public TaskWrapper asyncLater(long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().asyncLater(this, delayTicks));
    }

    public TaskWrapper asyncTimer(long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().asyncTimer(this, delayTicks, periodTicks));
    }

    public TaskWrapper runOnLocation(Location location) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnLocation(location, this));
    }

    public TaskWrapper runOnLocationLater(Location location, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnLocationLater(location, this, delayTicks));
    }

    public TaskWrapper runOnLocationTimer(Location location, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnLocationTimer(location, this, delayTicks, periodTicks));
    }

    public TaskWrapper runOnEntity(Entity entity) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnEntity(entity, this, this));
    }

    public TaskWrapper runOnEntityLater(Entity entity, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnEntityLater(entity, this, this, delayTicks));
    }

    public TaskWrapper runOnEntityTimer(Entity entity, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnEntityTimer(entity, this, this, delayTicks, periodTicks));
    }

    public void cancel() {
        if (this.taskWrapper == null)
            return;
        this.taskWrapper.cancel();
    }

    public boolean isCancelled() {
        if (this.taskWrapper == null) {
            return true;
        }
        return this.taskWrapper.isCancelled();
    }

    protected TaskWrapper setTaskWrapper(TaskWrapper taskWrapper) {
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
