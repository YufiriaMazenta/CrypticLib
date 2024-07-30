package crypticlib.action;

import crypticlib.action.impl.EmptyAction;
import crypticlib.action.impl.ErrorAction;
import crypticlib.action.impl.common.*;
import crypticlib.chat.BukkitMsgSender;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public enum ActionCompiler {

    INSTANCE;

    private final Map<String, Function<String, Action>> actionSupplierMap = new ConcurrentHashMap<>();

    ActionCompiler() {
        regAction("console", Console::new);
        regAction("command", Command::new);
        regAction("tell", Tell::new);
        regAction("title", Title::new);
        regAction("subtitle", Subtitle::new);
        regAction("actionbar", ActionBar::new);
        regAction("delay", Delay::new);
    }

    /**
     * 编译一条动作
     * @param actionStr 动作的文本
     * @return 编译完成的动作
     */
    public @NotNull Action compile(String actionStr) {
        if (actionStr == null || actionStr.isEmpty()) {
            return new EmptyAction();
        }
        int index = actionStr.indexOf(" ");
        if (index < 0) {
            if (actionSupplierMap.containsKey(actionStr)) {
                return actionSupplierMap.get(actionStr).apply(null);
            } else {
                BukkitMsgSender.INSTANCE.info("&cError when compile action: " + actionStr);
                return new ErrorAction(actionStr);
            }
        } else {
            String token = actionStr.substring(0, index);
            if (actionSupplierMap.containsKey(token)) {
                return actionSupplierMap.get(token).apply(actionStr.substring(index + 1));
            } else {
                BukkitMsgSender.INSTANCE.info("&cError when compile action: " + actionStr);
                return new ErrorAction(actionStr);
            }
        }
    }

    /**
     * 编译多条动作为动作链
     * @param actionStrList 动作文本列表
     * @return 编译完成的动作链首个动作
     */
    public Action compile(List<String> actionStrList) {
        if (actionStrList == null || actionStrList.isEmpty())
            return new EmptyAction();
        List<Action> actions = new ArrayList<>();
        for (String actionStr : actionStrList) {
            actions.add(compile(actionStr));
        }
        if (actions.size() < 2)
            return actions.get(0);
        for (int i = actions.size() - 1; i >= 1; i--) {
            Action pre = actions.get(i - 1);
            Action this_ = actions.get(i);
            pre.setNext(this_);
        }
        return actions.get(0);
    }

    public List<String> decompile(Action action) {
        List<String> actionStrList = new ArrayList<>();
        while (action.next() != null) {
            actionStrList.add(action.toActionStr());
            action = action.next();
        }
        return actionStrList;
    }

    public ActionCompiler regAction(String name, Function<String, Action> actionSupplier) {
        actionSupplierMap.put(name, actionSupplier);
        return this;
    }

    public Map<String, Function<String, Action>> actionMap() {
        return actionSupplierMap;
    }

}
