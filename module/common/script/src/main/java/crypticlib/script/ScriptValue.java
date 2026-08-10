package crypticlib.script;

import crypticlib.script.object.PropertyResolver;

import java.math.BigDecimal;
import java.util.Objects;

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
            SMALL_INTS[i] = new Int(i - 128, int.class);
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

    public static ScriptValue of(long value, boolean explicitLong) {
        if (!explicitLong && value >= -128 && value < 128) {
            return SMALL_INTS[(int) value + 128];
        }
        return new Int(value, explicitLong ? long.class : long.class);
    }

    public static ScriptValue of(long value) {
        if (value >= -128 && value < 128) {
            return SMALL_INTS[(int) value + 128];
        }
        return new Int(value, long.class);
    }

    public static ScriptValue of(int value) {
        if (value >= -128 && value < 128) {
            return SMALL_INTS[value + 128];
        }
        return new Int(value, int.class);
    }

    public static ScriptValue of(boolean value) {
        return Bool.of(value);
    }

    public static ScriptValue nil() {
        return NullValue.NIL;
    }

    public static ScriptValue of(Object value, PropertyResolver resolver) {
        if (value == null) return nil();
        if (value instanceof String) return of((String) value);
        if (value instanceof Integer) return of((Integer) value);
        if (value instanceof Long) return of((Long) value);
        if (value instanceof Double) return of((Double) value);
        if (value instanceof Float) return of((double) (Float) value);
        if (value instanceof Boolean) return of((Boolean) value);
        // BigDecimal 必须在 Number 之前判断，否则会走 doubleValue() 丢失精度
        if (value instanceof BigDecimal) return of((BigDecimal) value);
        if (value instanceof Number) return of(((Number) value).doubleValue());
        return new ObjectValue(value, resolver);
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

    public boolean isObject() {
        return this instanceof ObjectValue;
    }

    /**
     * 返回此值在方法匹配时代表的 Java 类型。
     * 用于反射方法缓存的类型签名 key：相同 actualType 的值总是映射到同一个缓存条目。
     */
    public Class<?> actualType() {
        if (this instanceof Str) return String.class;
        if (this instanceof Int) return ((Int) this).valueType();
        if (this instanceof Num) return double.class;
        if (this instanceof Bool) return boolean.class;
        if (this instanceof ObjectValue) {
            Object raw = ((ObjectValue) this).value();
            return raw != null ? raw.getClass() : Object.class;
        }
        return Void.class;  // NullValue
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
            String raw = ((Str) this).value();
            try {
                return new BigDecimal(raw);
            } catch (NumberFormatException e) {
                // 非数字字符串不再静默当 0：否则 "hello".abs() 之类会算出 0 而非报错
                throw new ScriptException("Cannot convert string \"" + raw + "\" to number");
            }
        }
        if (this instanceof Bool) {
            return ((Bool) this).value() ? BigDecimal.ONE : BigDecimal.ZERO;
        }
        // nil 参与算术同样报错，避免 nil + 1 得到 1
        throw new ScriptException("Cannot convert nil to number");
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
            String raw = ((Str) this).value();
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException e) {
                try {
                    return new BigDecimal(raw).longValue();
                } catch (NumberFormatException e2) {
                    throw new ScriptException("Cannot convert string \"" + raw + "\" to number");
                }
            }
        }
        if (this instanceof Bool) {
            return ((Bool) this).value() ? 1 : 0;
        }
        throw new ScriptException("Cannot convert nil to number");
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
        // nil 只与 nil 相等，与任意其他类型比较均视为不相等（避免 nil == 0 / nil == "" 误判为真）
        if (this.isNull() || other.isNull()) {
            if (this.isNull() && other.isNull()) return 0;
            return this.isNull() ? -1 : 1;
        }
        // 对象值只支持相等语义：用 equals 判定，不相等统一返回 1。
        // compare() 是 CMP_EQ/CMP_NEQ/CMP_GT 等的共用入口，无法区分调用来源，
        // 因此对象值的顺序比较（> < >= <=）结果无实际意义，只保证 == / != 正确。
        if (this.isObject() || other.isObject()) {
            if (!(this.isObject() && other.isObject())) return 1;
            Object a = ((ObjectValue) this).value();
            Object b = ((ObjectValue) other).value();
            return Objects.equals(a, b) ? 0 : 1;
        }
        if (this.isNumber() || other.isNumber()) {
            // 若一侧为非数字字符串，退回字符串比较，避免 "abc" == 0 误判为真
            if (this.isString() && !isNumericString(((Str) this).value())) {
                return this.asString().compareTo(other.asString());
            }
            if (other.isString() && !isNumericString(((Str) other).value())) {
                return this.asString().compareTo(other.asString());
            }
            // 如果都是整数类型，使用整数比较
            if (this.isInteger() && other.isInteger()) {
                return Long.compare(this.asLong(), other.asLong());
            }
            return this.asBigDecimal().compareTo(other.asBigDecimal());
        }
        return this.asString().compareTo(other.asString());
    }

    private static boolean isNumericString(String s) {
        try {
            new BigDecimal(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
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
        private final Class<?> valueType; // int.class 或 long.class

        public Int(long value, Class<?> valueType) {
            this.value = value;
            this.valueType = valueType;
        }

        public long value() {
            return value;
        }

        public Class<?> valueType() {
            return valueType;
        }

        @Override
        public String toString() {
            return "Int(" + value + (valueType == long.class ? "L" : "") + ")";
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

    public static final class ObjectValue extends ScriptValue {
        private final Object value;
        private final Class<?> type;
        private final PropertyResolver resolver;

        public ObjectValue(Object value, PropertyResolver resolver) {
            this.value = value;
            this.type = value == null ? null : value.getClass();
            this.resolver = resolver;
        }

        public Object value() {
            return value;
        }

        /**
         * 获取对象的类型
         *
         * @return 对象的类型，如果对象为null则返回null
         */
        public Class<?> type() {
            return type;
        }

        public PropertyResolver resolver() {
            return resolver;
        }

        @Override
        public String asString() {
            return value == null ? "" : value.toString();
        }

        @Override
        public boolean isNull() {
            return value == null;
        }

        @Override
        public boolean isTruthy() {
            return value != null;
        }

        // 对象值不能参与数值运算。父类的 fallback 会用 toString() 去解析，
        // 导致 ${event} + 1 静默得到 0 而非报错，因此这里显式拒绝。
        @Override
        public BigDecimal asBigDecimal() {
            throw new ScriptException("Cannot convert object " + typeName() + " to number");
        }

        @Override
        public long asLong() {
            throw new ScriptException("Cannot convert object " + typeName() + " to number");
        }

        @Override
        public boolean asBoolean() {
            return value != null;
        }

        public String typeName() {
            return type == null ? "null" : type.getName();
        }

        @Override
        public String toString() {
            return "Object(" + typeName() + ")";
        }
    }
}
