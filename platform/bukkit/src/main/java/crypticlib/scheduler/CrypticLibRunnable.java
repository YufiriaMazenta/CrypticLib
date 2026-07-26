package crypticlib.scheduler;

import crypticlib.CrypticLibBukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;

public abstract class CrypticLibRunnable implements Runnable {

    protected volatile TaskWrapper taskWrapper;

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
        return runOnEntity(entity, null);
    }

    /**
     * 在实体所在的region线程上执行任务。
     * <p>
     * retiredTask 对应 Folia 的 retired 回调 (实体已被移除时的通知), 而非"重试";
     * 默认不再把任务体自身当作 retired 回调, 以免实体移除时 run() 被额外执行一次。
     * 在 Spigot 上该回调语义由 SpigotScheduler 尽力模拟。
     *
     * @param entity      目标实体
     * @param retiredTask 实体被移除时执行的回调, 可为 null
     */
    public TaskWrapper runOnEntity(Entity entity, Runnable retiredTask) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnEntity(entity, this, retiredTask));
    }

    public TaskWrapper runOnEntityLater(Entity entity, long delayTicks) {
        return runOnEntityLater(entity, null, delayTicks);
    }

    /**
     * @see #runOnEntity(Entity, Runnable)
     */
    public TaskWrapper runOnEntityLater(Entity entity, Runnable retiredTask, long delayTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnEntityLater(entity, this, retiredTask, delayTicks));
    }

    public TaskWrapper runOnEntityTimer(Entity entity, long delayTicks, long periodTicks) {
        return runOnEntityTimer(entity, null, delayTicks, periodTicks);
    }

    /**
     * @see #runOnEntity(Entity, Runnable)
     */
    public TaskWrapper runOnEntityTimer(Entity entity, Runnable retiredTask, long delayTicks, long periodTicks) {
        checkTaskNotNull();
        return setTaskWrapper(CrypticLibBukkit.scheduler().runOnEntityTimer(entity, this, retiredTask, delayTicks, periodTicks));
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
