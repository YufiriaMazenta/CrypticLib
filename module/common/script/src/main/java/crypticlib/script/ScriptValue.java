package crypticlib.script;

/**
 * 脚本值的类型安全封装
 * 所有脚本内部运算都通过此类进行，避免 ClassCastException
 */
public abstract class ScriptValue {

    // ---- 小整数缓存 ----
    private static final Num[] SMALL_INTS = new Num[256];
    static {
        for (int i = 0; i < 256; i++) {
            SMALL_INTS[i] = new Num(i - 128);
        }
    }

    // ---- 工厂方法 ----
    public static ScriptValue of(String value) {
        return new Str(value);
    }

    public static ScriptValue of(double value) {
        int ivalue = (int) value;
        if (ivalue == value && ivalue >= -128 && ivalue < 128) {
            return SMALL_INTS[ivalue + 128];
        }
        return new Num(value);
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
        if (this instanceof Str) return ((Str) this).value();
        if (this instanceof Num) return String.valueOf(((Num) this).value());
        if (this instanceof Bool) return String.valueOf(((Bool) this).value());
        return "";  // nil 返回空字符串，避免插值时出现 "null"
    }

    public double asNumber() {
        if (this instanceof Num) return ((Num) this).value();
        if (this instanceof Str) {
            try {
                return Double.parseDouble(((Str) this).value());
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        if (this instanceof Bool) return ((Bool) this).value() ? 1 : 0;
        return 0;
    }

    public boolean asBoolean() {
        if (this instanceof Bool) return ((Bool) this).value();
        if (this instanceof Num) return ((Num) this).value() != 0;
        if (this instanceof Str) return Boolean.parseBoolean(((Str) this).value());
        return false;
    }

    // ---- 比较 ----
    public int compare(ScriptValue other) {
        if (this.isNumber() || other.isNumber()) {
            return Double.compare(this.asNumber(), other.asNumber());
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

    public static final class Num extends ScriptValue {
        private final double value;

        public Num(double value) {
            this.value = value;
        }

        public double value() {
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
