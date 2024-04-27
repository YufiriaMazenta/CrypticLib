package crypticlib.chat;

import crypticlib.CrypticLib;
import crypticlib.lang.entry.StringLangEntry;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天信息发送器
 */
@SuppressWarnings("deprecation")
public class MsgSender {

    /**
     * 发送语言文本给一个对象，此文本会处理颜色代码与papi变量
     * @param receiver 发送到的对象
     * @param msg 发送的语言
     */
    public static void sendMsg(@NotNull CommandSender receiver, StringLangEntry msg) {
        sendMsg(receiver, msg, new HashMap<>());
    }

    /**
     * 发送文本给一个对象，此文本会处理颜色代码和papi变量
     *
     * @param receiver 发送到的对象
     * @param msg      发送的消息
     */
    public static void sendMsg(@NotNull CommandSender receiver, String msg) {
        sendMsg(receiver, msg, new HashMap<>());
    }

    /**
     * 发送语言文本给一个对象，此文本会处理颜色代码与papi变量
     * @param receiver 发送到的对象
     * @param msg 发送的语言
     * @param replaceMap 需要替换的文本
     */
    public static void sendMsg(@NotNull CommandSender receiver, StringLangEntry msg, Map<String, String> replaceMap) {
        if (receiver instanceof Player) {
            sendMsg(receiver, msg.value((Player) receiver), replaceMap);
        } else {
            sendMsg(receiver, msg.value(), replaceMap);
        }
    }

    /**
     * 发送文本给一个对象，此文本会处理颜色代码和papi变量，并根据replaceMap的内容替换源文本
     *
     * @param receiver   发送到的对象
     * @param msg        发送的消息
     * @param replaceMap 需要替换的文本
     */
    public static void sendMsg(@NotNull CommandSender receiver, String msg, @NotNull Map<String, String> replaceMap) {
        if (msg == null)
            return;
        for (String formatStr : replaceMap.keySet()) {
            msg = msg.replace(formatStr, replaceMap.get(formatStr));
        }
        if (receiver instanceof Player)
            msg = TextProcessor.placeholder((Player) receiver, msg);
        sendMsg(receiver, TextProcessor.toComponent(TextProcessor.color(msg)));
    }

    /**
     * 发送多个Bungee聊天组件给接收者
     *
     * @param receiver       接收者
     * @param baseComponents bungee聊天组件
     */
    public static void sendMsg(@NotNull CommandSender receiver, @NotNull BaseComponent... baseComponents) {
        sendMsg(receiver, new TextComponent(baseComponents));
    }

    /**
     * 发送Bungee聊天组件给接收者
     *
     * @param receiver      接收者
     * @param baseComponent bungee聊天组件
     */
    public static void sendMsg(@NotNull CommandSender receiver, @NotNull BaseComponent baseComponent) {
        receiver.spigot().sendMessage(baseComponent);
    }

    public static void sendTitle(Player player, String title, StringLangEntry subTitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, title, subTitle.value(player), fadeIn, stay, fadeOut);
    }

    public static void sendTitle(Player player, StringLangEntry title, String subTitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, title.value(player), subTitle, fadeIn, stay, fadeOut);
    }

    public static void sendTitle(Player player, StringLangEntry title, StringLangEntry subTitle, int fadeIn, int stay, int fadeOut) {;
        sendTitle(player, title.value(player), subTitle.value(player), fadeIn, stay, fadeOut);
    }

    /**
     * 给玩家发送Title
     *
     * @param player   发送的玩家
     * @param title    发送的Title
     * @param subTitle 发送的Subtitle
     * @param fadeIn   Title的淡入时间
     * @param stay     Title的停留时间
     * @param fadeOut  Title的淡出时间
     */
    public static void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        if (player == null)
            return;
        if (title == null) {
            title = "";
        }
        if (subTitle == null) {
            subTitle = "";
        }
        title = TextProcessor.color(TextProcessor.placeholder(player, title));
        subTitle = TextProcessor.color(TextProcessor.placeholder(player, subTitle));
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    public static void sendTitle(Player player, String title, StringLangEntry subTitle) {
        sendTitle(player, title, subTitle.value(player));
    }

    public static void sendTitle(Player player, StringLangEntry title, String subTitle) {
        sendTitle(player, title.value(player), subTitle);
    }

    public static void sendTitle(Player player, StringLangEntry title, StringLangEntry subTitle) {
        sendTitle(player, title.value(player), subTitle.value(player));
    }

    /**
     * 给玩家发送Title
     *
     * @param player   发送的玩家
     * @param title    发送的Title
     * @param subTitle 发送的Subtitle
     */
    public static void sendTitle(Player player, String title, String subTitle) {
        sendTitle(player, title, subTitle, 10, 70, 20);
    }

    /**
     * 给玩家发送Action Bar
     *
     * @param player    发送的玩家
     * @param component 发送的ActionBar聊天组件
     */
    public static void sendActionBar(Player player, BaseComponent component) {
        if (player == null)
            return;
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    public static void sendActionBar(Player player, BaseComponent... components) {
        sendActionBar(player, new TextComponent(components));
    }

    public static void sendActionBar(Player player, StringLangEntry text) {
        sendActionBar(player, text.value(player));
    }

    /**
     * 给玩家发送Action Bar消息
     *
     * @param player 发送的玩家
     * @param text   发送的ActionBar文本
     */
    public static void sendActionBar(Player player, String text) {
        text = TextProcessor.color(TextProcessor.placeholder(player, text));
        sendActionBar(player, TextProcessor.toComponent(text));
    }

    public static void broadcast(StringLangEntry msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMsg(player, msg);
        }
        info(msg);
    }

    /**
     * 为所有玩家发送一条消息,这条消息会处理颜色代码和PlaceholderAPI变量
     *
     * @param msg 发送的消息
     */
    public static void broadcast(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMsg(player, msg);
        }
        info(msg);
    }

    public static void broadcastActionBar(StringLangEntry msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, msg);
        }
    }

    /**
     * 给所有玩家发送一条ActionBar位置的消息
     *
     * @param msg 发送的消息
     */
    public static void broadcastActionbar(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, msg);
        }
    }

    public static void broadcastTitle(String title, StringLangEntry subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public static void broadcastTitle(StringLangEntry title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public static void broadcastTitle(StringLangEntry title, StringLangEntry subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    /**
     * 给所有玩家发送一条title
     *
     * @param title    发送的title文本
     * @param subtitle 发送的subtitle文本
     * @param fadeIn   淡入时间
     * @param stay     停留时间
     * @param fadeOut  淡出时间
     */
    public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    public static void broadcastTitle(String title, StringLangEntry subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    public static void broadcastTitle(StringLangEntry title, String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    public static void broadcastTitle(StringLangEntry title, StringLangEntry subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    /**
     * 给所有玩家发送一条title
     *
     * @param title    发送的title文本
     * @param subtitle 发送的subtitle文本
     */
    public static void broadcastTitle(String title, String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    public static void debug(StringLangEntry msg) {
        debug(msg.value());
    }

    public static void debug(String msg) {
        if (CrypticLib.debug())
            info(msg);
    }

    public static void info(StringLangEntry msg) {
        sendMsg(Bukkit.getConsoleSender(), msg);
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码
     *
     * @param msg 发送的文本
     */
    public static void info(String msg) {
        sendMsg(Bukkit.getConsoleSender(), msg);
    }

    public static void info(StringLangEntry msg, Map<String, String> replaceMap) {
        info(msg.value(), replaceMap);
    }

    /**
     * 向后台发送一条DEBUG文本，此文本只会在
     * @param msg 发送的文本
     * @param replaceMap 需要替换的文本
     */
    public static void debug(StringLangEntry msg, Map<String, String> replaceMap) {
        if (CrypticLib.debug())
            info("[DEBUG] | " + msg.value(), replaceMap);
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码，并根据replaceMap的内容替换源文本
     *
     * @param msg        发送的文本
     * @param replaceMap 需要替换的文本
     */
    public static void info(String msg, Map<String, String> replaceMap) {
        sendMsg(Bukkit.getConsoleSender(), msg, replaceMap);
    }

    /**
     * 向后台发送一条DEBUG文本，此文本只会在
     * @param msg 发送的文本
     * @param replaceMap 需要替换的文本
     */
    public static void debug(String msg, Map<String, String> replaceMap) {
        if (CrypticLib.debug())
            info("[DEBUG] | " + msg, replaceMap);
    }

}
