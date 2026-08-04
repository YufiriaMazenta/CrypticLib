package crypticlib.chat;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import crypticlib.*;
import crypticlib.lifecycle.LifecycleTaskSettings;
import crypticlib.lifecycle.Lifecycle;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.LifecycleRule;
import crypticlib.util.StringHelper;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@LifecycleTaskSettings(
    rules = @LifecycleRule(lifeCycle = Lifecycle.LOAD),
    platforms = PlatformSide.VELOCITY
)
public enum VelocityMsgSender implements MsgSender.ComponentSender<Component>, LifecycleTask {

    INSTANCE;

    private VelocityPlugin plugin;

    /**
     * 惰性获取插件实例,避免依赖 LifeCycle.LOAD 注入时机——
     * 在 scanJar 的 debug 输出、INIT 阶段等更早路径上也能安全使用,不再抛 NPE
     */
    private VelocityPlugin plugin() {
        if (plugin == null) {
            plugin = (VelocityPlugin) CrypticLib.plugin();
        }
        return plugin;
    }

    @Override
    public void sendMsg(Invoker receiver, @NotNull Component... baseComponents) {
        if (receiver == null)
            return;
        Component component = Component.text().build();
        for (Component baseComponent : baseComponents) {
            component = component.append(baseComponent);
        }
        ((CommandSource) receiver.platformInvoker()).sendMessage(component);
    }

    @Override
    public void sendMsg(Invoker receiver, @NotNull Component baseComponent) {
        if (receiver == null)
            return;
        ((CommandSource) receiver.platformInvoker()).sendMessage(baseComponent);
    }

    @Override
    public void sendActionBar(CommonPlayer player, Component component) {
        if (player == null)
            return;
        player.getPlatformPlayer((uuid) -> plugin().getPlayer(uuid).orElse(null)).ifPresent(vcPlayer -> {
            vcPlayer.sendActionBar(component);
        });
    }

    @Override
    public void sendActionBar(CommonPlayer player, Component... components) {
        if (player == null)
            return;
        Component component = Component.text().build();
        for (Component baseComponent : components) {
            component = component.append(baseComponent);
        }
        Component finalComponent = component;
        player.getPlatformPlayer((uuid) -> plugin().getPlayer(uuid).orElse(null)).ifPresent(vcPlayer -> {
            vcPlayer.sendActionBar(finalComponent);
        });
    }

    @Override
    public void broadcast(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (Player player : plugin().proxyServer().getAllPlayers()) {
            sendMsg(VelocityPlayer.byPlayer(player), msg);
        }
        info(msg);
    }

    @Override
    public void broadcastActionbar(String msg, Map<String, String> replaceMap) {
        msg = StringHelper.replaceStrings(msg, replaceMap);
        for (Player player : plugin().proxyServer().getAllPlayers()) {
            sendActionBar(VelocityPlayer.byPlayer(player), msg);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (Player player : plugin().proxyServer().getAllPlayers()) {
            sendTitle(VelocityPlayer.byPlayer(player), title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    @Override
    public void broadcastTitle(String title, String subtitle, Map<String, String> replaceMap) {
        title = StringHelper.replaceStrings(title, replaceMap);
        subtitle = StringHelper.replaceStrings(subtitle, replaceMap);
        for (Player player : plugin().proxyServer().getAllPlayers()) {
            sendTitle(VelocityPlayer.byPlayer(player), title, subtitle);
        }
    }

    @Override
    public void info(String msg, Map<String, String> replaceMap) {
        msg = "&7[" + CrypticLib.plugin().pluginName() + "] " + msg;
        sendMsg(VelocityInvoker.byCommandSource(plugin().proxyServer().getConsoleCommandSource()), msg, replaceMap);
    }

    @Override
    public void lifecycle(CrypticLibPlugin plugin, Lifecycle lifeCycle) {
        this.plugin = (VelocityPlugin) plugin;
    }

}
