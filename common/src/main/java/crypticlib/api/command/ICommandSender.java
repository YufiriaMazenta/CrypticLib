package crypticlib.api.command;

import crypticlib.api.IPlayer;
import net.md_5.bungee.api.chat.BaseComponent;

import java.util.UUID;

/**
 * 包装命令执行者，用于跨平台执行命令
 */
public interface ICommandSender {

    String getName();

    void sendMessage(String message);

    void sendMessage(String... message);

    void sendMessage(UUID uuid, String message);

    void sendMessage(UUID uuid, String... message);

    void sendMessage(BaseComponent message);

    void sendMessage(BaseComponent... message);

    void sendMessage(UUID uuid, BaseComponent message);

    void sendMessage(UUID uuid, BaseComponent... message);

    boolean hasPermission(String permission);

    boolean isOp();

    void setOp(boolean value);

    default boolean isConsole() {
        return !isPlayer();
    }

    default boolean isPlayer() {
        return this instanceof IPlayer;
    }

    boolean dispatchCommand(String command);

    Object getPlatformCommandSender();
    
}
