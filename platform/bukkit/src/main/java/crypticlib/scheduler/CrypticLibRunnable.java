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

    public ITaskWrapper sync() {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().sync(this));
    }

    public ITaskWrapper syncLater(long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().syncLater(this, delayTicks));
    }

    public ITaskWrapper syncTimer(long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().syncTimer(this, delayTicks, periodTicks));
    }

    public ITaskWrapper async(Plugin plugin) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().async(this));
    }

    public ITaskWrapper asyncLater(long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().asyncLater(this, delayTicks));
    }

    public ITaskWrapper asyncTimer(long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().asyncTimer(this, delayTicks, periodTicks));
    }

    public ITaskWrapper runOnLocation(Location location) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runOnLocation(location, this));
    }

    public ITaskWrapper runOnLocationLater(Location location, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runOnLocationLater(location, this, delayTicks));
    }

    public ITaskWrapper runOnLocationTimer(Location location, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runOnLocationTimer(location, this, delayTicks, periodTicks));
    }

    public ITaskWrapper runOnEntity(Entity entity) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runOnEntity(entity, this, this));
    }

    public ITaskWrapper runOnEntityLater(Entity entity, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runOnEntityLater(entity, this, this, delayTicks));
    }

    public ITaskWrapper runOnEntityTimer(Entity entity, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.platform().scheduler().runOnEntityTimer(entity, this, this, delayTicks, periodTicks));
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
