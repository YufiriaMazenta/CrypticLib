package crypticlib.api.command;

public interface ICommandManager {

    void registerCommand(ICommandHandler handler, CommandInfo commandInfo);

    void unregisterCommand(CommandInfo commandInfo);

    void unregisterAllCommands();

}
