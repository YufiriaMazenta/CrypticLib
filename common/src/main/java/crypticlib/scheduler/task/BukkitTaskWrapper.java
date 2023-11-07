package crypticlib.scheduler.task;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class BukkitTaskWrapper implements ITaskWrapper {

    private final BukkitTask bukkitTask;

    public BukkitTaskWrapper(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }

    @Override
    public Plugin owner() {
        return bukkitTask.getOwner();
    }

    @Override
    public int taskId() {
        return bukkitTask.getTaskId();
    }

    @Override
    public boolean isCancelled() {
        return bukkitTask.isCancelled();
    }

    @Override
    public BukkitTask platformTask() {
        return bukkitTask;
    }

}
