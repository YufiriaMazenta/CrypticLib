package crypticlib.script.func;

import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;
import crypticlib.script.vm.ScriptVM;

/**
 * 数学函数模块
 * 提供常用的数学运算函数
 *
 * 使用方式：
 * - math.abs(-5)    // 带命名空间
 * - abs(-5)         // 简写（无冲突时可用）
 */
public enum MathScriptModule implements ScriptModule {

    INSTANCE;

    @Override
    public String moduleName() {
        return "math";
    }

    @Override
    public void register(ScriptFunctionRegistry registry) {
        String module = moduleName();
        registry.register(module, "abs", this::abs);
        registry.register(module, "min", this::min);
        registry.register(module, "max", this::max);
        registry.register(module, "round", this::round);
        registry.register(module, "floor", this::floor);
        registry.register(module, "ceil", this::ceil);
        registry.register(module, "sqrt", this::sqrt);
        registry.register(module, "pow", this::pow);
        registry.register(module, "random", this::random);
    }

    /**
     * abs(number) → 返回绝对值
     */
    private ScriptValue abs(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        return ScriptValue.of(Math.abs(args[0].asNumber()));
    }

    /**
     * min(a, b) → 返回较小值
     */
    private ScriptValue min(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 2) return ScriptValue.nil();
        return ScriptValue.of(Math.min(args[0].asNumber(), args[1].asNumber()));
    }

    /**
     * max(a, b) → 返回较大值
     */
    private ScriptValue max(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 2) return ScriptValue.nil();
        return ScriptValue.of(Math.max(args[0].asNumber(), args[1].asNumber()));
    }

    /**
     * round(number) → 四舍五入
     */
    private ScriptValue round(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        return ScriptValue.of(Math.round(args[0].asNumber()));
    }

    /**
     * floor(number) → 向下取整
     */
    private ScriptValue floor(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        return ScriptValue.of(Math.floor(args[0].asNumber()));
    }

    /**
     * ceil(number) → 向上取整
     */
    private ScriptValue ceil(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        return ScriptValue.of(Math.ceil(args[0].asNumber()));
    }

    /**
     * sqrt(number) → 平方根
     */
    private ScriptValue sqrt(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) return ScriptValue.nil();
        double value = args[0].asNumber();
        if (value < 0) return ScriptValue.nil();
        return ScriptValue.of(Math.sqrt(value));
    }

    /**
     * pow(base, exponent) → 幂运算
     */
    private ScriptValue pow(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 2) return ScriptValue.nil();
        return ScriptValue.of(Math.pow(args[0].asNumber(), args[1].asNumber()));
    }

    /**
     * random() → 返回 [0, 1) 的随机数
     * random(max) → 返回 [0, max) 的随机整数
     * random(min, max) → 返回 [min, max) 的随机整数
     */
    private ScriptValue random(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length == 0) {
            return ScriptValue.of(Math.random());
        }
        if (args.length == 1) {
            int max = (int) args[0].asNumber();
            return ScriptValue.of((int) (Math.random() * max));
        }
        int min = (int) args[0].asNumber();
        int max = (int) args[1].asNumber();
        if (min >= max) return ScriptValue.of(min);
        return ScriptValue.of(min + (int) (Math.random() * (max - min)));
    }
}
