package crypticlib;

import crypticlib.api.command.ICommandManager;
import crypticlib.api.scheduler.IScheduler;
import org.jetbrains.annotations.NotNull;

public final class CrypticLib {

    private static IScheduler scheduler;
    private static ICommandManager commandManager;

    public static @NotNull IScheduler getScheduler() {
        return scheduler;
    }

    public static void setScheduler(@NotNull IScheduler scheduler) {
        CrypticLib.scheduler = scheduler;
    }

    public static ICommandManager getCommandManager() {
        return commandManager;
    }

    public static void setCommandManager(ICommandManager commandManager) {
        CrypticLib.commandManager = commandManager;
    }

}
