package crypticlib.impl.command;

import crypticlib.api.command.CommandHandler;
import crypticlib.api.command.CommandInfo;
import crypticlib.api.command.ICommandManager;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;

@PlatformSide(platform = Platform.BUKKIT)
public enum BukkitCommandManager implements ICommandManager {

    INSTANCE;

    @Override
    public void registerCommand(CommandHandler handler, CommandInfo commandInfo) {

    }

    @Override
    public void unregisterCommand(CommandInfo commandInfo) {

    }

    @Override
    public void unregisterAllCommands() {

    }
}
