package crypticlib.script.func;

import crypticlib.CrypticLib;
import crypticlib.chat.BukkitMsgSender;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;
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
    }

    private ScriptValue command(ScriptContext ctx, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Player player = ctx.player();
        String cmd = BukkitTextProcessor.placeholder(player, args[0].asString());
        return ScriptValue.of(Bukkit.dispatchCommand(player, cmd));
    }

    private ScriptValue console(ScriptContext ctx, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.of(false);
        Player player = ctx.player();
        String cmd = BukkitTextProcessor.placeholder(player, args[0].asString());
        return ScriptValue.of(Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd));
    }

    private ScriptValue tell(ScriptContext ctx, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Player player = ctx.player();
        String msg = BukkitTextProcessor.placeholder(player, args[0].asString());
        BukkitMsgSender.INSTANCE.sendMsg(player, msg);
        return ScriptValue.nil();
    }

    private ScriptValue actionbar(ScriptContext ctx, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Player player = ctx.player();
        String msg = BukkitTextProcessor.placeholder(player, args[0].asString());
        BukkitMsgSender.INSTANCE.sendActionBar(player, msg);
        return ScriptValue.nil();
    }

    private ScriptValue title(ScriptContext ctx, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        Player player = ctx.player();
        String title = BukkitTextProcessor.placeholder(player, args[0].asString());
        String subtitle = args.length > 1 ? BukkitTextProcessor.placeholder(player, args[1].asString()) : "";
        BukkitMsgSender.INSTANCE.sendTitle(player, title, subtitle, 10, 70, 20);
        return ScriptValue.nil();
    }

    private ScriptValue closeInv(ScriptContext ctx, ScriptValue... args) {
        ctx.player().closeInventory();
        return ScriptValue.nil();
    }


}
