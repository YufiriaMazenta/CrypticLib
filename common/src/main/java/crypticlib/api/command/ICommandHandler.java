package crypticlib.api.command;

import java.util.List;

public interface ICommandHandler {

    void execute(ICommandSender sender, List<String> args);

    List<String> tabComplete(ICommandSender sender, List<String> args);

}
