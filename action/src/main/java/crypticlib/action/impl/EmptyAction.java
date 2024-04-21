package crypticlib.action.impl;


import crypticlib.action.Action;
import crypticlib.action.BaseAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * 空动作
 * 不做任何事情，且会中断动作链的执行
 */
public class EmptyAction implements Action {
    @Override
    public void run(Player player, Plugin plugin) {

    }

    @Override
    public Action next() {
        return null;
    }

    @Override
    public Action setNext(Action next) {
        return null;
    }

}
