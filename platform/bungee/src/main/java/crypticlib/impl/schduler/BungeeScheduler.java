package crypticlib.impl.schduler;

import crypticlib.CrypticLib;
import crypticlib.impl.schduler.task.BungeeTaskWrapper;
import crypticlib.internal.BungeeUtil;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.api.scheduler.IScheduler;
import crypticlib.api.scheduler.task.ITaskWrapper;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * 在BungeeScheduler中，没有同步任务，所有的方法都将执行异步任务
 */
@PlatformSide(platform = Platform.BUNGEE)
public enum BungeeScheduler implements IScheduler {

    INSTANCE;

    @Override
    public ITaskWrapper runTask(@NotNull Runnable task) {
        return new BungeeTaskWrapper(ProxyServer.getInstance().getScheduler().runAsync(BungeeUtil.getPluginIns(), task));
    }

    @Override
    public ITaskWrapper runTaskAsync(@NotNull Runnable task) {
        return new BungeeTaskWrapper(ProxyServer.getInstance().getScheduler().runAsync(BungeeUtil.getPluginIns(), task));
    }

    @Override
    public ITaskWrapper runTaskLater(@NotNull Runnable task, long delayTicks) {
        return new BungeeTaskWrapper(ProxyServer.getInstance().getScheduler().schedule(BungeeUtil.getPluginIns(), task, delayTicks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public ITaskWrapper runTaskLaterAsync(@NotNull Runnable task, long delayTicks) {
        return new BungeeTaskWrapper(ProxyServer.getInstance().getScheduler().schedule(BungeeUtil.getPluginIns(), task, delayTicks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public ITaskWrapper runTaskTimer(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BungeeTaskWrapper(ProxyServer.getInstance().getScheduler().schedule(BungeeUtil.getPluginIns(), task, delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public ITaskWrapper runTaskTimerAsync(@NotNull Runnable task, long delayTicks, long periodTicks) {
        return new BungeeTaskWrapper(ProxyServer.getInstance().getScheduler().schedule(BungeeUtil.getPluginIns(), task, delayTicks * 50, periodTicks * 50, TimeUnit.MILLISECONDS));
    }

    @Override
    public void cancelTasks() {
        ProxyServer.getInstance().getScheduler().cancel(BungeeUtil.getPluginIns());
    }
    
}
