package crypticlib.scheduler;

import com.velocitypowered.api.scheduler.ScheduledTask;
import com.velocitypowered.api.scheduler.TaskStatus;

public class VelocityTaskWrapper implements TaskWrapper {

    private final ScheduledTask scheduledTask;

    public VelocityTaskWrapper(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
    }

    @Override
    public void cancel() {
        scheduledTask.cancel();
    }

    @Override
    public boolean isCancelled() {
        return scheduledTask.status() == TaskStatus.CANCELLED;
    }

}
