package crypticlib.script.object;

import crypticlib.script.ScriptException;
import crypticlib.script.ScriptValue;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 反射桥的回归测试: 泛型擦除后的 Object 形参、枚举形参、重载解析的确定性
 */
class ReflectBridgeTest {

    private final ReflectPropertyResolver resolver = ReflectPropertyResolver.INSTANCE;

    @Test
    void collectionAddAcceptsScriptValues() {
        List<String> list = new ArrayList<>();
        resolver.callMethod(list, "add", ScriptValue.of("x"));
        resolver.callMethod(list, "add", ScriptValue.of("y"));
        Assertions.assertEquals(java.util.Arrays.asList("x", "y"), list);
    }

    @Test
    void mapPutAcceptsScriptValues() {
        Map<String, String> map = new HashMap<>();
        resolver.callMethod(map, "put", ScriptValue.of("k"), ScriptValue.of("v"));
        Assertions.assertEquals(Collections.singletonMap("k", "v"), map);
    }

    @Test
    void enumParameterAcceptsExactName() {
        Holder holder = new Holder();
        resolver.callMethod(holder, "setUnit", ScriptValue.of("SECONDS"));
        Assertions.assertEquals(TimeUnit.SECONDS, holder.unit);
    }

    @Test
    void enumParameterAcceptsCaseInsensitiveName() {
        Holder holder = new Holder();
        resolver.callMethod(holder, "setUnit", ScriptValue.of("seconds"));
        Assertions.assertEquals(TimeUnit.SECONDS, holder.unit);
    }

    @Test
    void unknownEnumConstantIsRejected() {
        Holder holder = new Holder();
        Assertions.assertThrows(ScriptException.class,
            () -> resolver.callMethod(holder, "setUnit", ScriptValue.of("NO_SUCH_UNIT")));
        Assertions.assertNull(holder.unit);
    }

    @Test
    void overloadResolutionPrefersDoubleForFloatNumber() {
        StringBuilder builder = new StringBuilder();
        resolver.callMethod(builder, "append", ScriptValue.of(2.5));
        //若按 getMethods() 顺序误选 append(int)/append(boolean)/append(String), 结果会变成 "2" / "true" / "2.5" 之外的值
        Assertions.assertEquals("2.5", builder.toString());
    }

    @Test
    void overloadResolutionPrefersIntForIntValue() {
        StringBuilder builder = new StringBuilder();
        resolver.callMethod(builder, "append", ScriptValue.of(7));
        Assertions.assertEquals("7", builder.toString());
    }

    @Test
    void overloadResolutionPrefersLongForLongValue() {
        StringBuilder builder = new StringBuilder();
        resolver.callMethod(builder, "append", ScriptValue.of(5_000_000_000L));
        Assertions.assertEquals("5000000000", builder.toString());
    }

    @Test
    void overloadResolutionPrefersStringForStringValue() {
        StringBuilder builder = new StringBuilder();
        resolver.callMethod(builder, "append", ScriptValue.of("text"));
        Assertions.assertEquals("text", builder.toString());
    }

    @Test
    void byteAndShortParametersRangeCheck() {
        Holder holder = new Holder();
        resolver.callMethod(holder, "setTiny", ScriptValue.of(12));
        Assertions.assertEquals((byte) 12, holder.tiny);
        Assertions.assertThrows(ScriptException.class,
            () -> resolver.callMethod(holder, "setTiny", ScriptValue.of(400)));
    }

    @Test
    void charParameterRequiresSingleCharacter() {
        Holder holder = new Holder();
        resolver.callMethod(holder, "setMark", ScriptValue.of("A"));
        Assertions.assertEquals('A', holder.mark);
        Assertions.assertThrows(ScriptException.class,
            () -> resolver.callMethod(holder, "setMark", ScriptValue.of("AB")));
    }

    @Test
    void asIntRejectsOutOfRangeValue() {
        ScriptValue big = ScriptValue.of(4_000_000_000L);
        Assertions.assertThrows(ScriptException.class, big::asInt);
        Assertions.assertEquals(5, ScriptValue.of(5).asInt());
    }

    /**
     * 测试用的接收方
     */
    public static final class Holder {
        private TimeUnit unit;
        private byte tiny;
        private char mark;

        public void setUnit(TimeUnit unit) {
            this.unit = unit;
        }

        public void setTiny(byte tiny) {
            this.tiny = tiny;
        }

        public void setMark(char mark) {
            this.mark = mark;
        }
    }
}
