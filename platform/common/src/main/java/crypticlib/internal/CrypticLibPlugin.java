package crypticlib.internal;

import crypticlib.PlatformSide;
import crypticlib.command.CommandManager;

public interface CrypticLibPlugin {

    String pluginName();

    CommandManager<?, ?> commandManager();

    PlatformSide platform();

}
