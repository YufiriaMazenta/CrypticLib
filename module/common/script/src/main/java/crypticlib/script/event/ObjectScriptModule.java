package crypticlib.script.event;

import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptException;
import crypticlib.script.ScriptValue;
import crypticlib.script.func.ScriptFunctionRegistry;
import crypticlib.script.func.ScriptModule;
import crypticlib.script.vm.ScriptVM;

/**
 * 对象操作脚本函数模块
 * 提供通用的 Java 对象属性访问和方法调用能力，通过方法调用语法使用:
 * <pre>
 *   ${player}.get("name")
 *   ${event}.get("player").get("name")
 *   ${event}.set("cancelled", true)
 *   ${player}.invoke("sendMessage", "Hello!")
 *   ${event}.invoke("setCancelled", true)
 * </pre>
 * receiver 为 nil 时统一返回 nil，使链式访问可安全传播；
 * receiver 类型不对或参数个数不足时抛 {@link ScriptException}。
 */
public enum ObjectScriptModule implements ScriptModule {

    INSTANCE;

    @Override
    public String moduleName() {
        return "obj";
    }

    @Override
    public void register(ScriptFunctionRegistry registry) {
        String module = moduleName();
        registry.register(module, "get", this::getProperty);
        registry.register(module, "set", this::setProperty);
        registry.register(module, "invoke", this::invoke);
    }

    /**
     * ${obj}.get("field") → 读取属性
     * args[0] = receiver (ObjectValue), args[1] = 属性名
     */
    private ScriptValue getProperty(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "get", 2, "get(name)");
        if (obj == null) return ScriptValue.nil();
        return obj.resolver().getProperty(obj.value(), args[1].asString());
    }

    /**
     * ${obj}.set("field", value) → 设置属性
     * args[0] = receiver, args[1] = 属性名, args[2] = 值
     */
    private ScriptValue setProperty(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "set", 3, "set(name, value)");
        if (obj == null) return ScriptValue.nil();
        obj.resolver().setProperty(obj.value(), args[1].asString(), args[2]);
        return ScriptValue.nil();
    }

    /**
     * ${obj}.invoke("methodName", arg1, arg2, ...) → 调用方法
     * args[0] = receiver (ObjectValue), args[1] = 方法名, args[2..n] = 方法参数
     */
    private ScriptValue invoke(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "invoke", 2, "invoke(method, args...)");
        if (obj == null) return ScriptValue.nil();
        String methodName = args[1].asString();
        ScriptValue[] methodArgs = new ScriptValue[args.length - 2];
        System.arraycopy(args, 2, methodArgs, 0, methodArgs.length);
        return obj.resolver().callMethod(obj.value(), methodName, methodArgs);
    }

    /**
     * 校验并取出 receiver。
     * 顺序很重要：先判 nil 再判类型，否则 nil receiver 会误报类型错误，
     * 破坏 ${maybeNull}.get("x") 的安全传播语义。
     *
     * @return receiver，若为 nil 则返回 null 表示调用方应直接返回 nil
     */
    private ScriptValue.ObjectValue receiver(ScriptValue[] args, String funcName, int minArgs, String usage) {
        if (args.length < 1) {
            // 只有当作普通函数直接调用（obj.get() 而非 ${x}.get()）才可能到这里
            throw new ScriptException(funcName + "() requires an object receiver");
        }
        if (args[0].isNull()) {
            return null;
        }
        if (!(args[0] instanceof ScriptValue.ObjectValue)) {
            throw new ScriptException(funcName + "() requires an object receiver, got " + args[0]);
        }
        if (args.length < minArgs) {
            throw new ScriptException(usage + " requires " + (minArgs - 1) + " argument(s), got " + (args.length - 1));
        }
        return (ScriptValue.ObjectValue) args[0];
    }
}
