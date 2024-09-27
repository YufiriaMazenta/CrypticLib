package crypticlib.chat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import crypticlib.lifecycle.AutoTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import crypticlib.lifecycle.VelocityLifeCycleTask;
import crypticlib.VelocityPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.util.Ticks;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@AutoTask(
    rules = @TaskRule(lifeCycle = LifeCycle.INIT)
)
public enum VelocityMsgSender implements MsgSender<CommandSource, Component, Player>, VelocityLifeCycleTask {

    INSTANCE;

    private VelocityPlugin plugin;

    @Override
    public void sendMsg(CommandSource receiver, String msg, @NotNull Map<String, String> replaceMap) {
        if (receiver == null)
            return;
        for (String key : replaceMap.keySet()) {
            msg = msg.replace(key, replaceMap.get(key));
        }
        Component component = VelocityTextProcessor.toComponent(msg);
        receiver.sendMessage(component);
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
    public void sendTitle(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        if (player == null)
            return;
        Title titleObj = buildTitle(title, subtitle, fadeIn, stay, fadeOut, replaceMap);
        player.showTitle(titleObj);
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
    public void sendActionBar(Player player, String text, Map<String, String> replaceMap) {
        if (player == null)
            return;
        for (String formatStr : replaceMap.keySet()) {
            text = text.replace(formatStr, replaceMap.get(formatStr));
        }
        Component component = VelocityTextProcessor.toComponent(text);
        player.sendActionBar(component);
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        for (String formatStr : replaceMap.keySet()) {
            msg = msg.replace(formatStr, replaceMap.get(formatStr));
        }
        Component component = VelocityTextProcessor.toComponent(msg);
        for (Player player : plugin.proxyServer().getAllPlayers()) {
            player.sendMessage(component);
        }
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        for (String formatStr : replaceMap.keySet()) {
            msg = msg.replace(formatStr, replaceMap.get(formatStr));
        }
        Component component = VelocityTextProcessor.toComponent(msg);
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
        for (String formatStr : replaceMap.keySet()) {
            msg = msg.replace(formatStr, replaceMap.get(formatStr));
        }
        Component component = VelocityTextProcessor.toComponent(msg);
        plugin.proxyServer().getConsoleCommandSource().sendMessage(component);
    }

    @Override
    public void run(VelocityPlugin plugin, LifeCycle lifeCycle) {
        this.plugin = plugin;
    }

    private Title buildTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        for (String key : replaceMap.keySet()) {
            title = title.replace(key, replaceMap.get(key));
            subtitle = subtitle.replace(key, replaceMap.get(key));
        }
        Component titleComponent = VelocityTextProcessor.toComponent(title);
        Component subTitleComponent = VelocityTextProcessor.toComponent(subtitle);
        return Title.title(titleComponent, subTitleComponent, Title.Times.times(Ticks.duration(fadeIn), Ticks.duration(stay), Ticks.duration(fadeOut)));
    }

}
