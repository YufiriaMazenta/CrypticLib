package crypticlib.chat;

import crypticlib.util.StringHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum BukkitMsgSender implements MsgSender<CommandSender, BaseComponent, Player> {

    INSTANCE;

    @Override
    public void sendMsg(CommandSender receiver, String msg, @NotNull Map<String, String> replaceMap) {
        if (receiver == null)
            return;
        if (msg == null)
            return;
        msg = StringHelper.replaceStrings(msg, replaceMap);
        if (receiver instanceof Player)
            msg = BukkitTextProcessor.placeholder((Player) receiver, msg);
        sendMsg(receiver, BukkitTextProcessor.toComponent(BukkitTextProcessor.color(msg)));
    }

    @Override
    public void sendMsg(CommandSender receiver, @NotNull BaseComponent... baseComponents) {
        sendMsg(receiver, new TextComponent(baseComponents));
    }

    @Override
    public void sendMsg(CommandSender receiver, @NotNull BaseComponent baseComponent) {
        if (receiver == null)
            return;
        receiver.spigot().sendMessage(baseComponent);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
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
        title = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player, title));
        subTitle = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player, subTitle));
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    @Override
    public void sendActionBar(Player player, BaseComponent component) {
        if (player == null)
            return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    @Override
    public void sendActionBar(Player player, BaseComponent... baseComponents) {
        sendActionBar(player, new TextComponent(baseComponents));
    }

    @Override
    public void sendActionBar(Player player, String text, Map<String, String> replaceMap) {
        text = StringHelper.replaceStrings(text, replaceMap);
        text = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player, text));
        sendActionBar(player, BukkitTextProcessor.toComponent(text));
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMsg(player, msg);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, msg);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        sendMsg(Bukkit.getConsoleSender(), msg, replaceMap);
    }

}
