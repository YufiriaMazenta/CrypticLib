package crypticlib;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 定义执行/调用某种行为的实体
 * 用于命令,脚本等模块
 */
public interface Invoker {

    @NotNull Object getPlatformInvoker();

    @NotNull String getName();

    default void sendMsg(String msg) {
        sendMsg(msg, new HashMap<>());
    }

    void sendMsg(String msg, Map<String, String> replaceMap);

    boolean hasPermission(String permission);

    boolean isPlayer();

    boolean isConsole();

    CommonPlayer asPlayer();

    InvokerType invokerType();

    enum InvokerType {
        PLAYER,
        ENTITY,
        CONSOLE,
        BLOCK
    }

}
