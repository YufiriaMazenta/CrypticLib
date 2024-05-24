package crypticlib.api.command;

import java.util.List;

public interface CommandHandler {

    void execute(WrappedCommandSender<?> sender, List<String> args);

    List<String> tabComplete(WrappedCommandSender<?> sender, List<String> args);

}
