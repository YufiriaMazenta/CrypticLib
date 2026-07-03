package crypticlib.scheduler.task;

import crypticlib.scheduler.TaskWrapper;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface BukkitTaskWrapper extends TaskWrapper {

    @NotNull
    Plugin owner();

    Integer taskId();

    @NotNull
    Object platformTask();

}
