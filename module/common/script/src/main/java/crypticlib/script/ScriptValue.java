package crypticlib.script;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * 脚本值的类型安全封装
 * 所有脚本内部运算都通过此类进行，避免 ClassCastException
 */
public abstract class ScriptValue {

    /** 除法运算的默认精度 */
    public static final int DIV_SCALE = 10;

    // ---- 小整数缓存 ----
    private static final Int[] SMALL_INTS = new Int[256];
    static {
        for (int i = 0; i < 256; i++) {
            SMALL_INTS[i] = new Int(i - 128);
        }
    }

    // ---- 工厂方法 ----
    public static ScriptValue of(String value) {
        return new Str(value);
    }

    public static ScriptValue of(double value) {
        return new Num(BigDecimal.valueOf(value));
    }

    public static ScriptValue of(BigDecimal value) {
        return new Num(value);
    }

    public static ScriptValue of(long value) {
        if (value >= -128 && value < 128) {
            return SMALL_INTS[(int) value + 128];
        }
        return new Int(value);
    }

    public static ScriptValue of(int value) {
        if (value >= -128 && value < 128) {
            return SMALL_INTS[value + 128];
        }
        return new Int(value);
    }

    public static ScriptValue of(boolean value) {
        return Bool.of(value);
    }

    public static ScriptValue nil() {
        return NullValue.NIL;
    }

    // ---- 类型判断 ----
    public boolean isString() {
        return this instanceof Str;
    }

    public boolean isNumber() {
        return this instanceof Num || this instanceof Int;
    }

    public boolean isInteger() {
        return this instanceof Int;
    }

    public boolean isFloat() {
        return this instanceof Num;
    }

    public boolean isBoolean() {
        return this instanceof Bool;
    }

    public boolean isNull() {
        return this instanceof NullValue;
    }

    public boolean isTruthy() {
        return !isNull() && !(this instanceof Bool && !((Bool) this).value());
    }

    // ---- 取值 ----
    public String asString() {
        if (this instanceof Str) {
            return ((Str) this).value();
        }
        if (this instanceof Int) {
            return String.valueOf(((Int) this).value());
        }
        if (this instanceof Num) {
            return ((Num) this).value().toPlainString();
        }
        if (this instanceof Bool) {
            return String.valueOf(((Bool) this).value());
        }
        return "";  // nil 返回空字符串，避免插值时出现 "null"
    }

    public BigDecimal asBigDecimal() {
        if (this instanceof Int) {
            return BigDecimal.valueOf(((Int) this).value());
        }
        if (this instanceof Num) {
            return ((Num) this).value();
        }
        if (this instanceof Str) {
            try {
                return new BigDecimal(((Str) this).value());
            } catch (NumberFormatException e) {
                return BigDecimal.ZERO;
            }
        }
        if (this instanceof Bool) {
            return ((Bool) this).value() ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    public double asNumber() {
        return asBigDecimal().doubleValue();
    }

    public long asLong() {
        if (this instanceof Int) {
            return ((Int) this).value();
        }
        if (this instanceof Num) {
            return ((Num) this).value().longValue();
        }
        if (this instanceof Str) {
            try {
                return Long.parseLong(((Str) this).value());
            } catch (NumberFormatException e) {
                try {
                    return new BigDecimal(((Str) this).value()).longValue();
                } catch (NumberFormatException e2) {
                    return 0;
                }
            }
        }
        if (this instanceof Bool) {
            return ((Bool) this).value() ? 1 : 0;
        }
        return 0;
    }

    public int asInt() {
        return (int) asLong();
    }

    public boolean asBoolean() {
        if (this instanceof Bool) {
            return ((Bool) this).value();
        }
        if (this instanceof Int) {
            return ((Int) this).value() != 0;
        }
        if (this instanceof Num) {
            return ((Num) this).value().compareTo(BigDecimal.ZERO) != 0;
        }
        if (this instanceof Str) {
            return Boolean.parseBoolean(((Str) this).value());
        }
        return false;
    }

    // ---- 比较 ----
    public int compare(ScriptValue other) {
        if (this.isNumber() || other.isNumber()) {
            // 如果都是整数类型，使用整数比较
            if (this.isInteger() && other.isInteger()) {
                return Long.compare(this.asLong(), other.asLong());
            }
            return this.asBigDecimal().compareTo(other.asBigDecimal());
        }
        return this.asString().compareTo(other.asString());
    }

    // ---- 具体类型 ----

    public static final class Str extends ScriptValue {
        private final String value;

        public Str(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return "Str(" + value + ")";
        }
    }

    public static final class Int extends ScriptValue {
        private final long value;

        public Int(long value) {
            this.value = value;
        }

        public long value() {
            return value;
        }

        @Override
        public String toString() {
            return "Int(" + value + ")";
        }
    }

    public static final class Num extends ScriptValue {
        private final BigDecimal value;

        public Num(BigDecimal value) {
            this.value = value;
        }

        public BigDecimal value() {
            return value;
        }

        @Override
        public String toString() {
            return "Num(" + value + ")";
        }
    }

    public static final class Bool extends ScriptValue {
        private static final Bool TRUE = new Bool(true);
        private static final Bool FALSE = new Bool(false);
        private final boolean value;

        private Bool(boolean value) {
            this.value = value;
        }

        public static Bool of(boolean v) {
            return v ? TRUE : FALSE;
        }

        public boolean value() {
            return value;
        }

        @Override
        public String toString() {
            return "Bool(" + value + ")";
        }
    }

    public static final class NullValue extends ScriptValue {

        public static final ScriptValue NIL = new NullValue();

        private NullValue() {
        }

        @Override
        public String toString() {
            return "Null";
        }
    }
}
