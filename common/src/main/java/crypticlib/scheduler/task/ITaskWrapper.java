package crypticlib.scheduler.task;

import org.bukkit.plugin.Plugin;

public interface ITaskWrapper {

    void cancel();

    Plugin owner();

    int taskId();

    boolean isCancelled();

    /**
     * 返回对应平台的原始Task类型
     * @return 对应平台的原始Task类型
     */
    Object platformTask();

}
