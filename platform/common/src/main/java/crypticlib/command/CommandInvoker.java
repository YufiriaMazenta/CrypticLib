package crypticlib.command;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public interface CommandInvoker {

    Object getPlatformInvoker();

    @NotNull String getName();

    default void sendMsg(String msg) {
        sendMsg(msg, null);
    }

    void sendMsg(String msg, Map<String, String> replaceMap);

    boolean hasPermission(String permission);

    boolean isPlayer();

    boolean isConsole();

    PlayerCommandInvoker asPlayer();

}
