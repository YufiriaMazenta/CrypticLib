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
        return CrypticLib.pluginInstance().pluginName().toLowerCase() + "_builtin";
    }

    @Override
    public void register(ScriptFunctionRegistry registry) {
        registry.register("delay", this::delay);
    }

    /**
     * delay(ticks) → 暂停脚本执行，延迟指定 tick 后继续执行后续指令
     * 例：delay(20)  →  延迟 1 秒
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

}
