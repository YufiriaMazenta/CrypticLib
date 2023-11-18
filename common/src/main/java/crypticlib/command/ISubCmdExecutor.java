package crypticlib.command;

import java.util.ArrayList;
import java.util.List;

/**
 * CrypticLib提供的子命令接口
 */
public interface ISubCmdExecutor extends ICmdExecutor {

    /**
     * 获取此子命令的名字
     * @return 子命令的名字
     */
    String name();

    /**
     * 获取此子命令的别名
     * @return 子命令的别名
     */
    default List<String> aliases() {
        return new ArrayList<>();
    }

    /**
     * 获取此子命令所需的权限,如果不需要权限则返回null
     * @return 子命令所需权限
     */
    default String permission() {
        return null;
    }

}
