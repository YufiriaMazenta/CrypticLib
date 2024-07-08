package crypticlib.chat;

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
    public void sendMsg(@NotNull CommandSender receiver, String msg, @NotNull Map<String, String> replaceMap) {
        if (msg == null)
            return;
        for (String formatStr : replaceMap.keySet()) {
            msg = msg.replace(formatStr, replaceMap.get(formatStr));
        }
        if (receiver instanceof Player)
            msg = BukkitTextProcessor.placeholder((Player) receiver, msg);
        sendMsg(receiver, BukkitTextProcessor.toComponent(BukkitTextProcessor.color(msg)));
    }

    @Override
    public void sendMsg(@NotNull CommandSender receiver, @NotNull BaseComponent... baseComponents) {
        sendMsg(receiver, new TextComponent(baseComponents));
    }

    @Override
    public void sendMsg(@NotNull CommandSender receiver, @NotNull BaseComponent baseComponent) {
        receiver.spigot().sendMessage(baseComponent);
    }

    @Override
    public void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (player == null)
            return;
        if (title == null) {
            title = "";
        }
        if (subTitle == null) {
            subTitle = "";
        }
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
    public void sendActionBar(Player player, String text) {
        text = BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player, text));
        sendActionBar(player, BukkitTextProcessor.toComponent(text));
    }

    @Override
    public void broadcast(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMsg(player, msg);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, msg);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        sendMsg(Bukkit.getConsoleSender(), msg);
    }

}
