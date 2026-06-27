package crypticlib.script.func;

import crypticlib.CrypticLib;
import crypticlib.chat.BukkitMsgSender;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;
import crypticlib.script.vm.ScriptVM;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
        registry.register("close-inv", this::closeInv);
        registry.register("delay", this::delay);
    }

    private ScriptValue command(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Player player = ctx.player();
        StringBuilder sb = new StringBuilder();
        for (ScriptValue arg : args) {
            sb.append(arg.asString());
        }
        String cmd = BukkitTextProcessor.placeholder(player, sb.toString());
        return ScriptValue.of(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    private ScriptValue console(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Player player = ctx.player();
        String cmd = BukkitTextProcessor.placeholder(player, args[0].asString());
        return ScriptValue.of(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    private ScriptValue tell(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Player player = ctx.player();
        StringBuilder sb = new StringBuilder();
        for (ScriptValue arg : args) {
            sb.append(arg.asString());
        }
        String msg = BukkitTextProcessor.placeholder(player, sb.toString());
        BukkitMsgSender.INSTANCE.sendMsg(player, msg);
        return ScriptValue.nil();
    }

    private ScriptValue actionbar(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Player player = ctx.player();
        StringBuilder sb = new StringBuilder();
        for (ScriptValue arg : args) {
            sb.append(arg.asString());
        }
        String msg = BukkitTextProcessor.placeholder(player, sb.toString());
        BukkitMsgSender.INSTANCE.sendActionBar(player, msg);
        return ScriptValue.nil();
    }

    private ScriptValue title(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Player player = ctx.player();
        String titleStr = BukkitTextProcessor.placeholder(player, args[0].asString());
        String subtitle = args.length > 1 ? BukkitTextProcessor.placeholder(player, args[1].asString()) : "";
        BukkitMsgSender.INSTANCE.sendTitle(player, titleStr, subtitle, 10, 70, 20);
        return ScriptValue.nil();
    }

    private ScriptValue closeInv(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ctx.player().closeInventory();
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

}
