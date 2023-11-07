package crypticlib.scheduler.task;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.plugin.Plugin;

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
    public Plugin owner() {
        return scheduledTask.getOwningPlugin();
    }

    @Override
    public int taskId() {
        throw new UnsupportedOperationException("Folia task can not get task id");
    }

    @Override
    public boolean isCancelled() {
        return scheduledTask.isCancelled();
    }

    @Override
    public ScheduledTask platformTask() {
        return scheduledTask;
    }

}
