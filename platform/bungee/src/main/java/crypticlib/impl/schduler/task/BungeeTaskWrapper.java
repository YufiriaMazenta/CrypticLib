package crypticlib.impl.schduler.task;

import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.api.scheduler.task.ITaskWrapper;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import org.jetbrains.annotations.NotNull;

@PlatformSide(platform = Platform.BUNGEE)
public class BungeeTaskWrapper implements ITaskWrapper {

    private final ScheduledTask scheduledTask;

    public BungeeTaskWrapper(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @Override
    public void cancel() {
        scheduledTask.cancel();
    }

    @Override
    public Integer taskId() {
        return scheduledTask.getId();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException("Bungee task does not have isCancelled method");
    }

    @Override
    public @NotNull Object platformTask() {
        return scheduledTask;
    }
}
