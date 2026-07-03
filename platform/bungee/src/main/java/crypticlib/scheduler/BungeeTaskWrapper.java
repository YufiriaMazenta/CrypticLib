package crypticlib.scheduler;

import net.md_5.bungee.api.scheduler.ScheduledTask;

import java.util.concurrent.ScheduledFuture;

public class BungeeTaskWrapper implements TaskWrapper {

    private final ScheduledTask scheduledTask;
    private final ScheduledFuture<?> future;
    private volatile boolean cancelled = false;

    public BungeeTaskWrapper(ScheduledTask scheduledTask) {
        this.scheduledTask = scheduledTask;
        this.future = null;
    }

    public BungeeTaskWrapper(ScheduledFuture<?> future) {
        this.scheduledTask = null;
        this.future = future;
    }

    @Override
    public void cancel() {
        cancelled = true;
        if (scheduledTask != null) {
            scheduledTask.cancel();
        }
        if (future != null) {
            future.cancel(false);
        }
    }

    @Override
    public boolean isCancelled() {
        if (cancelled) return true;
        if (future != null) return future.isCancelled();
        return false;
    }

}
