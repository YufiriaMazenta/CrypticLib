package crypticlib.scheduler;

import org.jetbrains.annotations.NotNull;

public interface Scheduler {

    TaskWrapper sync(@NotNull Runnable task);

    TaskWrapper async(@NotNull Runnable task);

    TaskWrapper syncLater(@NotNull Runnable task, long delayTicks);

    TaskWrapper asyncLater(@NotNull Runnable task, long delayTicks);

    TaskWrapper syncTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    TaskWrapper asyncTimer(@NotNull Runnable task, long delayTicks, long periodTicks);

    void cancelTasks();

}
