package crypticlib.chat;

import crypticlib.CrypticLib;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public interface MsgSender<Receiver, Component, Player> {

    /**
     * 发送文本给一个对象，此文本会处理颜色代码和papi变量
     *
     * @param receiver 发送到的对象
     * @param msg      发送的消息
     */
    default void sendMsg(Receiver receiver, String msg) {
        sendMsg(receiver, msg, new HashMap<>());
    }

    /**
     * 发送文本给一个对象，此文本会处理颜色代码和papi变量，并根据replaceMap的内容替换源文本
     *
     * @param receiver   发送到的对象
     * @param msg        发送的消息
     * @param replaceMap 需要替换的文本
     */
    void sendMsg(Receiver receiver, String msg, @NotNull Map<String, String> replaceMap);

    /**
     * 发送多个聊天组件给接收者
     *
     * @param receiver       接收者
     * @param baseComponents bungee聊天组件
     */
    void sendMsg(Receiver receiver, @NotNull Component... baseComponents);

    /**
     * 发送Bungee聊天组件给接收者
     *
     * @param receiver      接收者
     * @param baseComponent bungee聊天组件
     */
    void sendMsg(Receiver receiver, @NotNull Component baseComponent);

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
    default void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        sendTitle(player, title, subTitle, fadeIn, stay, fadeOut, new HashMap<>());
    }

    void sendTitle(Player player, String title, String subTitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap);

    /**
     * 给玩家发送Title
     *
     * @param player   发送的玩家
     * @param title    发送的Title
     * @param subTitle 发送的Subtitle
     */
    default void sendTitle(Player player, String title, String subTitle) {
        sendTitle(player, title, subTitle, 10, 70, 20);
    }

    /**
     * 给玩家发送Action Bar
     *
     * @param player    发送的玩家
     * @param component 发送的ActionBar聊天组件
     */
    void sendActionBar(Player player, Component component);

    /**
     * 给玩家发送Action Bar
     *
     * @param player    发送的玩家
     * @param components 发送的ActionBar聊天组件
     */
    void sendActionBar(Player player, Component... components);

    /**
     * 给玩家发送Action Bar消息
     *
     * @param player 发送的玩家
     * @param text   发送的ActionBar文本
     */
    default void sendActionBar(Player player, String text) {
        sendActionBar(player, text, new HashMap<>());
    }

    void sendActionBar(Player player, String text, Map<String, String> replaceMap);

    /**
     * 为所有玩家发送一条消息,这条消息会处理颜色代码和PlaceholderAPI变量
     *
     * @param msg 发送的消息
     */
    default void broadcast(String msg) {
        broadcast(msg, new HashMap<>());
    }

    /**
     * 为所有玩家发送一条消息,这条消息会处理颜色代码和PlaceholderAPI变量
     *
     * @param msg 发送的消息
     */
    void broadcast(String msg, Map<String, String> replaceMap);

    /**
     * 给所有玩家发送一条ActionBar位置的消息
     *
     * @param msg 发送的消息
     */
    default void broadcastActionbar(String msg) {
        broadcastActionbar(msg, new HashMap<>());
    }

    void broadcastActionbar(String msg, Map<String, String> replaceMap);

    /**
     * 给所有玩家发送一条title
     *
     * @param title    发送的title文本
     * @param subtitle 发送的subtitle文本
     * @param fadeIn   淡入时间
     * @param stay     停留时间
     * @param fadeOut  淡出时间
     */
    default void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        broadcastTitle(title, subtitle, fadeIn, stay, fadeOut, new HashMap<>());
    }

    void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap);

    /**
     * 给所有玩家发送一条title
     *
     * @param title    发送的title文本
     * @param subtitle 发送的subtitle文本
     */
    default void broadcastTitle(String title, String subtitle) {
        broadcastTitle(title, subtitle, new HashMap<>());
    }

    void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap);

    /**
     * 向后台发送一条DEBUG文本
     *
     * @param msg        发送的文本
     */
    default void debug(String msg) {
        debug(msg, new HashMap<>());
    }

    /**
     * 向后台发送一条DEBUG文本
     *
     * @param msg        发送的文本
     * @param replaceMap 需要替换的文本
     */
    default void debug(String msg, Map<String, String> replaceMap) {
        if (CrypticLib.debug()) {
            info("[DEBUG] | " + msg, replaceMap);
        }
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码
     *
     * @param msg 发送的文本
     */
    default void info(String msg) {
        info(msg, new HashMap<>());
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码，并根据replaceMap的内容替换源文本
     *
     * @param msg        发送的文本
     * @param replaceMap 需要替换的文本
     */
    void info(String msg, Map<String, String> replaceMap);

}
