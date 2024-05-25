package crypticlib.api;

import crypticlib.api.command.ICommandSender;

public interface IPlatformAdapter {

    ICommandSender adaptCommandSender(Object platformCommandSender);

    IPlayer adaptPlayer(Object platformPlayer);

}
