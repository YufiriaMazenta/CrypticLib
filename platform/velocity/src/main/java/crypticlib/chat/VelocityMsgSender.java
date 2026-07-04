package crypticlib.chat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import crypticlib.CrypticLib;
import crypticlib.PlatformSide;
import crypticlib.VelocityPlugin;
import crypticlib.command.CommandInvoker;
import crypticlib.CommonPlayer;
import crypticlib.command.VelocityCommandInvoker;
import crypticlib.VelocityPlayer;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.LifeCycleTask;
import crypticlib.lifecycle.TaskRule;
import crypticlib.util.StringHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@LifeCycleTaskSettings(
    rules = @TaskRule(lifeCycle = LifeCycle.LOAD),
    platforms = PlatformSide.VELOCITY
)
public enum VelocityMsgSender implements MsgSender.ComponentSender<Component>, LifeCycleTask {

    INSTANCE;

    private VelocityPlugin plugin;

    @Override
    public void sendMsg(CommandInvoker receiver, @NotNull Component... baseComponents) {
        if (receiver == null)
            return;
        Component component = Component.text().build();
        for (Component baseComponent : baseComponents) {
            component = component.append(baseComponent);
        }
        ((CommandSource) receiver.getPlatformInvoker()).sendMessage(component);
    }

    @Override
    public void sendMsg(CommandInvoker receiver, @NotNull Component baseComponent) {
        if (receiver == null)
            return;
        ((CommandSource) receiver.getPlatformInvoker()).sendMessage(baseComponent);
    }

    @Override
    public void sendActionBar(CommonPlayer player, Component component) {
        if (player == null)
            return;
        ((Player) player.getPlatformPlayer()).sendActionBar(component);
    }

    @Override
    public void sendActionBar(CommonPlayer player, Component... components) {
        if (player == null)
            return;
        Component component = Component.text().build();
        for (Component baseComponent : components) {
            component = component.append(baseComponent);
        }
        ((Player) player.getPlatformPlayer()).sendActionBar(component);
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            sendMsg(new VelocityPlayer(player), msg);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            sendActionBar(new VelocityPlayer(player), msg);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            sendTitle(new VelocityPlayer(player), title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            sendTitle(new VelocityPlayer(player), title, subtitle);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        msg = "&7[" + CrypticLib.pluginName() + "] " + msg;
        sendMsg(new VelocityCommandInvoker(plugin.proxyServer().getConsoleCommandSource()), msg, replaceMap);
    }

    @Override
    public void lifecycle(Object plugin, LifeCycle lifeCycle) {
        this.plugin = (VelocityPlugin) plugin;
    }

}
