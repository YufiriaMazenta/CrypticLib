package crypticlib.impl.scheduler.task;

import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.api.scheduler.task.ITaskWrapper;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

@PlatformSide(platform = Platform.BUKKIT)
public class FoliaTaskWrapper implements ITaskWrapper {

    private final ScheduledTask scheduledTask;

    public FoliaTaskWrapper(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @Override
    public void cancel() {
        scheduledTask.cancel();
    }

    @Override
    public Integer taskId() {
        throw new UnsupportedOperationException("Folia task can not get task id");
    }

    @Override
    public boolean isCancelled() {
        return scheduledTask.isCancelled();
    }

    @Override
    public @NotNull ScheduledTask platformTask() {
        return scheduledTask;
    }

}
