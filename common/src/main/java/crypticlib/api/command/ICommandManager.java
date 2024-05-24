package crypticlib.api.command;

public interface ICommandManager {

    void registerCommand(CommandHandler handler, CommandInfo commandInfo);

    void unregisterCommand(CommandInfo commandInfo);

    void unregisterAllCommands();

}
