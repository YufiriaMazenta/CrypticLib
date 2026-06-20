package crypticlib.script.func;

import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;

/**
 * 脚本函数接口
 * 所有内置函数和自定义函数都实现此接口
 */
public interface ScriptFunction {

    /**
     * 执行函数
     * @param context 执行上下文（含 Player、变量等）
     * @param args 参数列表
     * @return 返回值
     */
    ScriptValue execute(ScriptContext context, ScriptValue... args);

}
