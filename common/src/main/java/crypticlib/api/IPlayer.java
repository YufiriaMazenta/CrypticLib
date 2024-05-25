package crypticlib.api;

import crypticlib.api.command.ICommandSender;

import java.net.InetSocketAddress;
import java.util.UUID;

/**
 * 方法需要补充
 */
public interface IPlayer extends ICommandSender {

    UUID getUniqueId();

    String getDisplayName();

    String getLocale();

    InetSocketAddress getAddress();

    int getViewDistance();

    void sendTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut);

    void sendActionBar(String text);

    Object getPlatformPlayer();

}
