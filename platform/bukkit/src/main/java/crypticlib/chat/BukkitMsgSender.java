package crypticlib.chat;

import crypticlib.CrypticLib;
import crypticlib.command.BukkitCommandInvoker;
import crypticlib.BukkitPlayer;
import crypticlib.command.CommandInvoker;
import crypticlib.CommonPlayer;
import crypticlib.util.StringHelper;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public enum BukkitMsgSender implements MsgSender.ComponentSender<BaseComponent> {

    INSTANCE;

    @Override
    public void sendMsg(CommandInvoker receiver, @NotNull BaseComponent... baseComponents) {
        sendMsg(receiver, new TextComponent(baseComponents));
    }

    @Override
    public void sendMsg(CommandInvoker receiver, @NotNull BaseComponent baseComponent) {
        if (receiver == null)
            return;
        ((CommandSender) receiver.getPlatformInvoker()).spigot().sendMessage(baseComponent);
    }

    @Override
    public void sendActionBar(CommonPlayer player, BaseComponent component) {
        if (player == null)
            return;
        ((Player) player.getPlatformPlayer()).spigot().sendMessage(ChatMessageType.ACTION_BAR, component);
    }

    @Override
    public void sendActionBar(CommonPlayer player, BaseComponent... baseComponents) {
        sendActionBar(player, new TextComponent(baseComponents));
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendMsg(new BukkitPlayer(player), msg);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendActionBar(new BukkitPlayer(player), msg);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(new BukkitPlayer(player), title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (Player player : Bukkit.getOnlinePlayers()) {
            sendTitle(new BukkitPlayer(player), title, subtitle);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        msg = "&7[" + CrypticLib.pluginName() + "] " + msg;
        sendMsg(new BukkitCommandInvoker(Bukkit.getConsoleSender()), msg, replaceMap);
    }

}
