package crypticlib.util;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * 聊天相关的工具类
 */
@SuppressWarnings("deprecation")
public class MsgUtil {

    /**
     * 发送文本给一个对象，此文本会处理颜色代码和papi变量
     * @param receiver 发送到的对象
     * @param msg 发送的消息
     */
    public static void sendMsg(CommandSender receiver, String msg) {
        sendMsg(receiver, msg, new HashMap<>());
    }

    /**
     * 发送文本给一个对象，此文本会处理颜色代码和papi变量，并根据replaceMap的内容替换源文本
     * @param receiver 发送到的对象
     * @param msg 发送的消息
     * @param replaceMap 需要替换的文本
     */
    public static void sendMsg(CommandSender receiver, String msg, Map<String, String> replaceMap) {
        for (String formatStr : replaceMap.keySet()) {
            msg = msg.replace(formatStr, replaceMap.get(formatStr));
        }
        if (receiver instanceof Player)
            msg = TextUtil.placeholder((Player) receiver, msg);
        sendMsg(receiver, TextUtil.color(TextUtil.color(msg)));
    }

    /**
     * 发送多个Bungee聊天组件给接收者
     * @param receiver 接收者
     * @param baseComponents bungee聊天组件
     */
    public static void sendMsg(CommandSender receiver, BaseComponent... baseComponents) {
        sendMsg(receiver, new TextComponent(baseComponents));
    }

    /**
     * 发送Bungee聊天组件给接收者
     * @param receiver 接收者
     * @param baseComponent bungee聊天组件
     */
    public static void sendMsg(CommandSender receiver, BaseComponent baseComponent) {
        if (receiver == null)
            return;
        receiver.spigot().sendMessage(baseComponent);
    }

    /**
     * 给玩家发送Title
     * @param player 发送的玩家
     * @param title 发送的Title
     * @param subTitle 发送的Subtitle
     * @param fadeIn Title的淡入时间
     * @param stay Title的停留时间
     * @param fadeOut Title的淡出时间
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
        title = TextUtil.color(TextUtil.placeholder(player, title));
        subTitle = TextUtil.color(TextUtil.placeholder(player, subTitle));
        player.sendTitle(title, subTitle, fadeIn, stay, fadeOut);
    }

    /**
     * 给玩家发送Title
     * @param player 发送的玩家
     * @param title 发送的Title
     * @param subTitle 发送的Subtitle
     */
    public static void sendTitle(Player player, String title, String subTitle) {
        sendTitle(player, title, subTitle, 10, 70, 20);
    }

    /**
     * 给玩家发送Action Bar
     * @param player 发送的玩家
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

    /**
     * 给玩家发送Action Bar消息
     * @param player 发送的玩家
     * @param text 发送的ActionBar文本
     */
    public static void sendActionBar(Player player, String text) {
        text = TextUtil.color(TextUtil.placeholder(player, text));
        sendActionBar(player, TextUtil.toComponent(text));
    }

    /**
     * 为所有玩家发送一条消息,这条消息会处理颜色代码和PlaceholderAPI变量
     * @param msg 发送的消息
     */
    public static void broadcast(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMsg(player, msg);
        }
        info(msg);
    }

    /**
     * 给所有玩家发送一条ActionBar位置的消息
     * @param msg 发送的消息
     */
    public static void broadcastActionbar(String msg) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(player, msg);
        }
    }

    /**
     * 给所有玩家发送一条title
     * @param title 发送的title文本
     * @param subtitle 发送的subtitle文本
     * @param fadeIn 淡入时间
     * @param stay 停留时间
     * @param fadeOut 淡出时间
     */
    public static void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    /**
     * 给所有玩家发送一条title
     * @param title 发送的title文本
     * @param subtitle 发送的subtitle文本
     */
    public static void broadcastTitle(String title, String subtitle) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(player, title, subtitle);
        }
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码
     * @param msg 发送的文本
     */
    public static void info(String msg) {
        sendMsg(Bukkit.getConsoleSender(), msg);
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码，并根据replaceMap的内容替换源文本
     * @param msg 发送的文本
     * @param replaceMap 需要替换的文本
     */
    public static void info(String msg, Map<String, String> replaceMap) {
        sendMsg(Bukkit.getConsoleSender(), msg, replaceMap);
    }

}
