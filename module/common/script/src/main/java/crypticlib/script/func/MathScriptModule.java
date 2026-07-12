package crypticlib.script.func;

import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptValue;
import crypticlib.script.vm.ScriptVM;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

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
        registry.register(module, "random_int", this::randomInt);
        registry.register(module, "int", this::toInt);
        registry.register(module, "float", this::toFloat);
    }

    /**
     * abs(number) → 返回绝对值
     */
    private ScriptValue abs(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        if (args[0].isInteger()) {
            return ScriptValue.of(Math.abs(args[0].asLong()));
        }
        return ScriptValue.of(args[0].asBigDecimal().abs());
    }

    /**
     * min(a, b) → 返回较小值
     */
    private ScriptValue min(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 2) {
            return ScriptValue.nil();
        }
        if (args[0].isInteger() && args[1].isInteger()) {
            return ScriptValue.of(Math.min(args[0].asLong(), args[1].asLong()));
        }
        return ScriptValue.of(args[0].asBigDecimal().min(args[1].asBigDecimal()));
    }

    /**
     * max(a, b) → 返回较大值
     */
    private ScriptValue max(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 2) {
            return ScriptValue.nil();
        }
        if (args[0].isInteger() && args[1].isInteger()) {
            return ScriptValue.of(Math.max(args[0].asLong(), args[1].asLong()));
        }
        return ScriptValue.of(args[0].asBigDecimal().max(args[1].asBigDecimal()));
    }

    /**
     * round(number) → 四舍五入，返回整数
     */
    private ScriptValue round(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        if (args[0].isInteger()) {
            return args[0];
        }
        return ScriptValue.of(args[0].asBigDecimal().setScale(0, RoundingMode.HALF_UP).toBigInteger().longValue());
    }

    /**
     * floor(number) → 向下取整
     */
    private ScriptValue floor(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        if (args[0].isInteger()) {
            return args[0];
        }
        return ScriptValue.of(args[0].asBigDecimal().setScale(0, RoundingMode.FLOOR).toBigInteger().longValue());
    }

    /**
     * ceil(number) → 向上取整
     */
    private ScriptValue ceil(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        if (args[0].isInteger()) {
            return args[0];
        }
        return ScriptValue.of(args[0].asBigDecimal().setScale(0, RoundingMode.CEILING).toBigInteger().longValue());
    }

    /**
     * sqrt(number) → 平方根
     */
    private ScriptValue sqrt(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        BigDecimal value = args[0].asBigDecimal();
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            return ScriptValue.nil();
        }
        return ScriptValue.of(value.sqrt(MathContext.DECIMAL128));
    }

    /**
     * pow(base, exponent) → 幂运算
     */
    private ScriptValue pow(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 2) {
            return ScriptValue.nil();
        }
        // BigDecimal.pow() 只支持整数指数
        int exponent = args[1].asInt();
        return ScriptValue.of(args[0].asBigDecimal().pow(exponent));
    }

    /**
     * random() → 返回 [0, 1) 的随机浮点数
     * random(max) → 返回 [0, max) 的随机浮点数
     * random(min, max) → 返回 [min, max) 的随机浮点数
     */
    private ScriptValue random(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length == 0) {
            return ScriptValue.of(BigDecimal.valueOf(Math.random()));
        }
        if (args.length == 1) {
            BigDecimal max = args[0].asBigDecimal();
            return ScriptValue.of(BigDecimal.valueOf(Math.random()).multiply(max));
        }
        BigDecimal min = args[0].asBigDecimal();
        BigDecimal max = args[1].asBigDecimal();
        if (min.compareTo(max) >= 0) {
            return ScriptValue.of(min);
        }
        BigDecimal range = max.subtract(min);
        return ScriptValue.of(min.add(BigDecimal.valueOf(Math.random()).multiply(range)));
    }

    /**
     * random_int(max) → 返回 [0, max) 的随机整数
     * random_int(min, max) → 返回 [min, max) 的随机整数
     */
    private ScriptValue randomInt(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length == 0) {
            return ScriptValue.nil();
        }
        if (args.length == 1) {
            long max = args[0].asLong();
            return ScriptValue.of((long) (Math.random() * max));
        }
        long min = args[0].asLong();
        long max = args[1].asLong();
        if (min >= max) {
            return ScriptValue.of(min);
        }
        return ScriptValue.of(min + (long) (Math.random() * (max - min)));
    }

    /**
     * int(value) → 转换为整数（截断小数部分）
     */
    private ScriptValue toInt(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        return ScriptValue.of(args[0].asLong());
    }

    /**
     * float(value) → 转换为浮点数
     */
    private ScriptValue toFloat(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        if (args.length < 1) {
            return ScriptValue.nil();
        }
        return ScriptValue.of(args[0].asBigDecimal());
    }
}
