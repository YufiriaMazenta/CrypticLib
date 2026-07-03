package crypticlib.chat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import crypticlib.CrypticLib;
import crypticlib.PlatformSide;
import crypticlib.VelocityPlugin;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.LifeCycleTask;
import crypticlib.lifecycle.TaskRule;
import crypticlib.util.StringHelper;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@LifeCycleTaskSettings(
    rules = @TaskRule(lifeCycle = LifeCycle.LOAD),
    platforms = PlatformSide.VELOCITY
)
public enum VelocityMsgSender implements MsgSender.ComponentSender<CommandSource, Component, Player>, LifeCycleTask {

    INSTANCE;

    private VelocityPlugin plugin;

    @Override
    public void sendMsg(Object receiver, String msg, @NotNull Map<String, String> replaceMap) {
        if (receiver == null)
            return;
        CommandSource source = (CommandSource) receiver;
        msg = StringHelper.replaceStrings(msg, replaceMap);
        Component component = VelocityTextProcessor.deserializeLegacyText(msg);
        source.sendMessage(component);
    }

    @Override
    public void sendMsg(CommandSource receiver, @NotNull Component... baseComponents) {
        if (receiver == null)
            return;
        Component component = Component.text().build();
        for (Component baseComponent : baseComponents) {
            component = component.append(baseComponent);
        }
        receiver.sendMessage(component);
    }

    @Override
    public void sendMsg(CommandSource receiver, @NotNull Component baseComponent) {
        if (receiver == null)
            return;
        receiver.sendMessage(baseComponent);
    }

    @Override
    public void sendTitle(Object player, String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        if (player == null)
            return;
        Player velocityPlayer = (Player) player;
        Title titleObj = buildTitle(title, subtitle, fadeIn, stay, fadeOut, replaceMap);
        velocityPlayer.showTitle(titleObj);
    }

    @Override
    public void sendActionBar(Player player, Component component) {
        player.sendActionBar(component);
    }

    @Override
    public void sendActionBar(Player player, Component... components) {
        if (player == null)
            return;
        Component component = Component.text().build();
        for (Component baseComponent : components) {
            component = component.append(baseComponent);
        }
        player.sendActionBar(component);
    }

    @Override
    public void sendActionBar(Object player, String text, Map<String, String> replaceMap) {
        if (player == null)
            return;
        Player velocityPlayer = (Player) player;
        text = StringHelper.replaceStrings(text, replaceMap);
        Component component = VelocityTextProcessor.deserializeLegacyText(text);
        velocityPlayer.sendActionBar(component);
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        Component component = VelocityTextProcessor.deserializeLegacyText(msg);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            player.sendMessage(component);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        Component component = VelocityTextProcessor.deserializeLegacyText(msg);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            player.sendActionBar(component);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        Title titleObj = buildTitle(title, subtitle, fadeIn, stay, fadeOut, replaceMap);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            player.showTitle(titleObj);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap) {
        Title titleObj = buildTitle(title, subtitle, 10, 70, 20, replaceMap);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            player.showTitle(titleObj);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        msg = "&7[" + CrypticLib.pluginName() + "] " + msg;
        msg = StringHelper.replaceStrings(msg, replaceMap);
        Component component = VelocityTextProcessor.deserializeLegacyText(msg);
        plugin.proxyServer().getConsoleCommandSource().sendMessage(component);
    }

    @Override
    public void lifecycle(Object plugin, LifeCycle lifeCycle) {
        this.plugin = (VelocityPlugin) plugin;
    }

    private Title buildTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        Component titleComponent = VelocityTextProcessor.deserializeLegacyText(title);
        Component subTitleComponent = VelocityTextProcessor.deserializeLegacyText(subtitle);
        return Title.title(titleComponent, subTitleComponent, Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut)));
    }

}
