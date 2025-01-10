package crypticlib.action.impl;

import crypticlib.action.BaseAction;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ErrorAction extends BaseAction {

    private final String actionStr;

    public ErrorAction(String actionStr) {
        this.actionStr = actionStr;
    }

    @Override
    public String toActionStr() {
        return actionStr;
    }

    @Override
    public void run(Player executor, @NotNull Plugin plugin, Map<String, String> args) {
        runNext(executor, plugin, args);
    }

}
