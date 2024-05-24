package crypticlib.impl.scheduler.task;

import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.api.scheduler.task.ITaskWrapper;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

@PlatformSide(platform = Platform.BUKKIT)
public class BukkitTaskWrapper implements ITaskWrapper {

    private final BukkitTask bukkitTask;

    public BukkitTaskWrapper(@NotNull BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    @Override
    public void cancel() {
        bukkitTask.cancel();
    }

    @Override
    public Integer taskId() {
        return bukkitTask.getTaskId();
    }

    @Override
    public boolean isCancelled() {
        return bukkitTask.isCancelled();
    }

    @Override
    @NotNull
    public BukkitTask platformTask() {
        return bukkitTask;
    }

}
