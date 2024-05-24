package crypticlib.api.command;

import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

/**
 * 包装命令执行者，用于跨平台执行命令
 */
public interface WrappedCommandSender<T> {

    void sendMessage(String message);

    void sendMessage(String... message);

    void sendMessage(UUID uuid, String message);

    void sendMessage(UUID uuid, String... message);

    void sendMessage(BaseComponent message);

    void sendMessage(BaseComponent... message);

    void sendMessage(UUID uuid, BaseComponent message);

    void sendMessage(UUID uuid, BaseComponent... message);

    boolean isOp();

    void setOp(boolean value);

    boolean hasPermission(String permission);

    boolean isConsole();

    boolean isPlayer();

    T getPlatformSender();
    
}
