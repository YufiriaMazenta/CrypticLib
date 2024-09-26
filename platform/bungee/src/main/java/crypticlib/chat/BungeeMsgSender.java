package crypticlib.chat;

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
    public void sendMsg(@NotNull CommandSender receiver, String msg, @NotNull Map<String, String> replaceMap) {
        if (msg == null)
            return;
        for (String formatStr : replaceMap.keySet()) {
            msg = msg.replace(formatStr, replaceMap.get(formatStr));
        }
        sendMsg(receiver, BungeeTextProcessor.toComponent(BungeeTextProcessor.color(msg)));
    }

    @Override
    public void sendMsg(@NotNull CommandSender receiver, @NotNull BaseComponent... baseComponents) {
        sendMsg(receiver, new TextComponent(baseComponents));
    }

    @Override
    public void sendMsg(@NotNull CommandSender receiver, @NotNull BaseComponent baseComponent) {
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
        for (String key : replaceMap.keySet()) {
            title = title.replace(key, replaceMap.get(key));
            subTitle = subTitle.replace(key, replaceMap.get(key));
        }
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
        for (String formatStr : replaceMap.keySet()) {
            text = text.replace(formatStr, replaceMap.get(formatStr));
        }
        text = BungeeTextProcessor.color(text);
        sendActionBar(player, BungeeTextProcessor.toComponent(text));
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        for (String replace : replaceMap.keySet()) {
            msg = msg.replace(replace, replaceMap.get(replace));
        }
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendMsg(player, msg);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        for (String replace : replaceMap.keySet()) {
            msg = msg.replace(replace, replaceMap.get(replace));
        }
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendActionBar(player, msg);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        for (String replace : replaceMap.keySet()) {
            title = title.replace(replace, replaceMap.get(replace));
            subtitle = subtitle.replace(replace, replaceMap.get(replace));
        }
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap) {
        for (String replace : replaceMap.keySet()) {
            title = title.replace(replace, replaceMap.get(replace));
            subtitle = subtitle.replace(replace, replaceMap.get(replace));
        }
        for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        sendMsg(ProxyServer.getInstance().getConsole(), msg);
    }

}
