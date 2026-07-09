package crypticlib.script.func;

import crypticlib.CrypticLib;
import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;
import crypticlib.script.vm.ScriptVM;

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
        registry.register("delay", this::delay);
        registry.register("set", this::set);
        registry.register("context", this::context);
    }

    /**
     * delay <tick数>
     * 暂停脚本执行，延迟指定 tick 后继续执行后续指令
     * 例：delay 20  →  延迟 1 秒
     */
    private ScriptValue delay(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        long ticks = args[0].asLong();
        if (ticks <= 0) {
            return ScriptValue.nil();
        }
        vm.pauseAndScheduleResume(ticks);
        return ScriptValue.nil();
    }

    /**
     * set("key", value) → 往上下文添加变量
     */
    private ScriptValue set(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 2) {
            return ScriptValue.nil();
        }
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
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        String key = args[0].asString();
        ScriptValue var = ctx.getVariable(key);
        if (var == null) {
            return ScriptValue.nil();
        }
        return var;
    }

}
