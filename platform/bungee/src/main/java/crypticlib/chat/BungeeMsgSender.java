package crypticlib.chat;

import crypticlib.CrypticLib;
import crypticlib.BungeeInvoker;
import crypticlib.BungeePlayer;
import crypticlib.Invoker;
import crypticlib.CommonPlayer;
import crypticlib.util.StringHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum BungeeMsgSender implements MsgSender.ComponentSender<BaseComponent> {

    INSTANCE;

    @Override
    public void sendMsg(Invoker receiver, @NotNull BaseComponent... baseComponents) {
        sendMsg(receiver, new TextComponent(baseComponents));
    }

    @Override
    public void sendMsg(Invoker receiver, @NotNull BaseComponent baseComponent) {
        if (receiver == null)
            return;
        ((CommandSender) receiver.getPlatformInvoker()).sendMessage(baseComponent);
    }

    @Override
    public void sendActionBar(CommonPlayer player, BaseComponent component) {
        if (player == null)
            return;
        player.getPlatformPlayer(ProxyServer.getInstance()::getPlayer).ifPresent(bungeePlayer -> {
            bungeePlayer.sendMessage(ChatMessageType.ACTION_BAR, component);
        });
    }

    @Override
    public void sendActionBar(CommonPlayer player, BaseComponent... baseComponents) {
        sendActionBar(player, new TextComponent(baseComponents));
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendMsg(BungeePlayer.byProxiedPlayer(player), msg);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendActionBar(BungeePlayer.byProxiedPlayer(player), msg);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendTitle(BungeePlayer.byProxiedPlayer(player), title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendTitle(BungeePlayer.byProxiedPlayer(player), title, subtitle);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        msg = "&7[" + CrypticLib.pluginName() + "] " + msg;
        sendMsg(BungeeInvoker.byCommandSender(ProxyServer.getInstance().getConsole()), msg, replaceMap);
    }

}
