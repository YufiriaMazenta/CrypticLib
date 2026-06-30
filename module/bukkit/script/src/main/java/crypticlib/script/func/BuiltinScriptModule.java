package crypticlib.script.func;

import crypticlib.CrypticLib;
import crypticlib.chat.BukkitMsgSender;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;
import crypticlib.script.vm.ScriptVM;
import crypticlib.util.IOHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.StringJoiner;

/**
 * 内置的脚本函数模块
 */
public enum BuiltinScriptModule implements ScriptModule {

    INSTANCE;

    @Override
    public String moduleName() {
        return CrypticLib.pluginName().toLowerCase() + "_builtin";
    }

    @Override
    public void register(ScriptFunctionRegistry registry) {
        registry.register("command", this::command);
        registry.register("console", this::console);
        registry.register("tell", this::tell);
        registry.register("actionbar", this::actionbar);
        registry.register("title", this::title);
        registry.register("log", this::log);
        registry.register("close-inv", this::closeInv);
        registry.register("delay", this::delay);
        registry.register("set", this::set);
        registry.register("context", this::context);
    }

    private ScriptValue command(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Optional<Player> playerOptional = ctx.onlinePlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            StringJoiner stringJoiner = new StringJoiner(" ");
            for (ScriptValue arg : args) {
                stringJoiner.add(arg.asString());
            }
            String cmd = BukkitTextProcessor.placeholder(player, stringJoiner.toString());
            return ScriptValue.of(Bukkit.dispatchCommand(player, cmd));
        }
        return ScriptValue.of(false);
    }

    private ScriptValue console(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Optional<Player> playerOptional = ctx.onlinePlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            StringJoiner stringJoiner = new StringJoiner(" ");
            for (ScriptValue arg : args) {
                stringJoiner.add(arg.asString());
            }
            String cmd = BukkitTextProcessor.placeholder(player, stringJoiner.toString());
            return ScriptValue.of(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
        }
        return ScriptValue.of(false);
    }

    private ScriptValue tell(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOptional = ctx.onlinePlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            StringJoiner stringJoiner = new StringJoiner(" ");
            for (ScriptValue arg : args) {
                stringJoiner.add(arg.asString());
            }
            String msg = BukkitTextProcessor.placeholder(player, stringJoiner.toString());
            BukkitMsgSender.INSTANCE.sendMsg(player, msg);
        }
        return ScriptValue.nil();
    }

    private ScriptValue actionbar(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOptional = ctx.onlinePlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            StringJoiner stringJoiner = new StringJoiner(" ");
            for (ScriptValue arg : args) {
                stringJoiner.add(arg.asString());
            }
            String msg = BukkitTextProcessor.placeholder(player, stringJoiner.toString());
            BukkitMsgSender.INSTANCE.sendActionBar(player, msg);
        }
        return ScriptValue.nil();
    }

    private ScriptValue title(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOptional = ctx.onlinePlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            String titleStr = BukkitTextProcessor.placeholder(player, args[0].asString());
            String subtitle = args.length > 1 ? BukkitTextProcessor.placeholder(player, args[1].asString()) : "";
            BukkitMsgSender.INSTANCE.sendTitle(player, titleStr, subtitle, 10, 70, 20);
        }
        return ScriptValue.nil();
    }

    private ScriptValue log(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Optional<Player> playerOptional = ctx.onlinePlayer();
        if (playerOptional.isPresent()) {
            Player player = playerOptional.get();
            StringJoiner stringJoiner = new StringJoiner(" ");
            for (ScriptValue arg : args) {
                stringJoiner.add(arg.asString());
            }
            String msg = BukkitTextProcessor.placeholder(player, stringJoiner.toString());
            IOHelper.info(msg);
        }
        return ScriptValue.nil();
    }

    private ScriptValue closeInv(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ctx.onlinePlayer().ifPresent(Player::closeInventory);
        return ScriptValue.nil();
    }

    /**
     * delay <tick数>
     * 暂停脚本执行，延迟指定 tick 后继续执行后续指令
     * 例：delay 20  →  延迟 1 秒
     */
    private ScriptValue delay(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        long ticks = (long) args[0].asNumber();
        if (ticks <= 0) return ScriptValue.nil();
        vm.pauseAndScheduleResume(ticks);
        return ScriptValue.nil();
    }

    /**
     * set("key", value) → 往上下文添加变量
     */
    private ScriptValue set(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 2) return ScriptValue.nil();
        String key = args[0].asString();
        ScriptValue value = args[1];
        ctx.setVariable(key, value);
        return ScriptValue.nil();
    }

    /**
     * context("key") → 返回上下文变量值
     * 比较通过脚本运算符实现: context("damage") >= 10
     */
    private ScriptValue context(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        String key = args[0].asString();
        ScriptValue var = ctx.getVariable(key);
        if (var == null) return ScriptValue.nil();
        return var;
    }

}
