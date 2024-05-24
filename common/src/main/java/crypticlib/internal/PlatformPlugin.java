package crypticlib.internal;

import crypticlib.CrypticLib;
import crypticlib.api.command.CommandHandler;
import crypticlib.api.command.CommandInfo;
import crypticlib.api.command.annotation.Command;
import crypticlib.api.permission.PermissionDefault;
import crypticlib.internal.annotation.PlatformSide;
import crypticlib.internal.reflect.PluginScanner;
import crypticlib.internal.reflect.ReflectUtil;

import java.util.Arrays;
import java.util.List;

public interface PlatformPlugin {

    default void regCommands() {
        List<Class<CommandHandler>> commandClasses = PluginScanner.INSTANCE.getSubClasses(CommandHandler.class);
        for (Class<CommandHandler> commandClass : commandClasses) {
            try {
                if (!commandClass.isAnnotationPresent(Command.class)) {
                    continue;
                }
                if (commandClass.isAnnotationPresent(PlatformSide.class)) {
                    PlatformSide platformSide = commandClass.getAnnotation(PlatformSide.class);
                    Platform[] platforms = platformSide.platform();
                    if (!Arrays.asList(platforms).contains(Platform.getCurrent())) {
                        continue;
                    }
                }
                Command command = commandClass.getAnnotation(Command.class);
                String name = command.name();
                String permission = command.permission();
                PermissionDefault permissionDefault = command.permissionDefault();
                String[] aliases = command.aliases();
                CommandInfo commandInfo = new CommandInfo(name, permission, permissionDefault, aliases);
                CommandHandler commandHandler = ReflectUtil.getSingletonClassInstance(commandClass);
                CrypticLib.getCommandManager().registerCommand(commandHandler, commandInfo);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    void regListeners();

}
