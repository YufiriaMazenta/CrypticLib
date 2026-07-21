package crypticlib;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 定义执行/调用某种行为的实体
 * 用于命令,脚本等模块
 */
public interface Invoker {

    UUID CONSOLE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @NotNull Object getPlatformInvoker();

    @NotNull String getName();

    /**
     * 获得执行者的UUID，如果是控制台，那么将会返回Nil UUID
     * @return 获得执行者的UUID，如果是控制台，那么将会返回Nil UUID
     */
    default @NotNull UUID getUniqueId() {
        return CONSOLE_UUID;
    }

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
