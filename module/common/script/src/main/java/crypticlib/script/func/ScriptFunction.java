package crypticlib.script.func;

import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;
import crypticlib.script.vm.ScriptVM;

/**
 * 脚本函数接口
 * 所有内置函数和自定义函数都实现此接口
 */
public interface ScriptFunction {

    /**
     * 执行函数
     * @param context 执行上下文（含 Player、变量等）
     * @param vm 虚拟机实例（可用于 delay 等需要中断执行的场景）
     * @param args 参数列表
     * @return 返回值
     */
    ScriptValue execute(ScriptContext context, ScriptVM vm, ScriptValue... args);

}
