package crypticlib.chat;

import crypticlib.CrypticLib;
import crypticlib.util.StringHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum BungeeMsgSender implements MsgSender<CommandSender, BaseComponent, ProxiedPlayer> {

    INSTANCE;

    @Override
    public void sendMsg(CommandSender receiver, String msg, @NotNull Map<String, String> replaceMap) {
        if (receiver == null)
            return;
        if (msg == null)
            return;
        msg = StringHelper.replaceStrings(msg, replaceMap);
        sendMsg(receiver, BungeeTextProcessor.toComponent(BungeeTextProcessor.color(msg)));
    }

    @Override
    public void sendMsg(CommandSender receiver, @NotNull BaseComponent... baseComponents) {
        sendMsg(receiver, new TextComponent(baseComponents));
    }

    @Override
    public void sendMsg(CommandSender receiver, @NotNull BaseComponent baseComponent) {
        if (receiver == null)
            return;
        receiver.sendMessage(baseComponent);
    }

    @Override
    public void sendTitle(ProxiedPlayer player, String title, String subTitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        if (player == null)
            return;
        if (title == null) {
            title = "";
        }
        if (subTitle == null) {
            subTitle = "";
        }
        title = StringHelper.replaceStrings(title, replaceMap);
        subTitle = StringHelper.replaceStrings(subTitle, replaceMap);
        title = BungeeTextProcessor.color(title);
        subTitle = BungeeTextProcessor.color(subTitle);
        ProxyServer.getInstance().createTitle()
            .title(BungeeTextProcessor.toComponent(title))
            .subTitle(BungeeTextProcessor.toComponent(subTitle))
            .fadeIn(fadeIn)
            .fadeOut(fadeOut)
            .stay(stay)
            .send(player);
    }

    @Override
    public void sendActionBar(ProxiedPlayer player, BaseComponent component) {
        if (player == null)
            return;
        player.sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    @Override
    public void sendActionBar(ProxiedPlayer player, BaseComponent... baseComponents) {
        sendActionBar(player, new TextComponent(baseComponents));
    }

    @Override
    public void sendActionBar(ProxiedPlayer player, String text, Map<String, String> replaceMap) {
        if (player == null)
            return;
        text = StringHelper.replaceStrings(text, replaceMap);
        text = BungeeTextProcessor.color(text);
        sendActionBar(player, BungeeTextProcessor.toComponent(text));
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendMsg(player, msg);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendActionBar(player, msg);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        msg = "&7[" + CrypticLib.pluginName() + "] " + msg;
        sendMsg(ProxyServer.getInstance().getConsole(), msg, replaceMap);
    }

}
