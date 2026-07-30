package crypticlib.script.event;

import crypticlib.script.ScriptException;
import crypticlib.script.ScriptValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于反射的通用属性解析器
 * 查找顺序: getXxx() → isXxx() → Map.get() → public field
 * <p>
 * 错误语义（有意不对称）：
 * <ul>
 *   <li><b>读属性</b>不存在时返回 nil，使链式访问与条件判断可安全传播</li>
 *   <li><b>写属性</b>与<b>方法调用</b>找不到目标或执行失败时抛 {@link ScriptException}，
 *       因为它们带副作用，静默失败会让"事件没被取消"这类问题无从排查</li>
 * </ul>
 */
public class ReflectPropertyResolver implements PropertyResolver {

    public static final ReflectPropertyResolver INSTANCE = new ReflectPropertyResolver();

    /** 类 → (属性名 → getter)，Optional.empty() 用于负向缓存，避免缺失属性每次重跑反射 */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Optional<Method>>> getterCache = new ConcurrentHashMap<>();

    @Override
    public ScriptValue getProperty(Object target, String propertyName) {
        if (target == null) return ScriptValue.nil();
        Class<?> clazz = target.getClass();

        Method getter = findGetter(clazz, propertyName);
        if (getter != null) {
            return wrapResult(invokeMethod(target, getter, new Object[0], propertyName));
        }

        if (target instanceof Map) {
            return wrapResult(((Map<?, ?>) target).get(propertyName));
        }

        try {
            Field field = clazz.getField(propertyName);
            return wrapResult(field.get(target));
        } catch (NoSuchFieldException ignored) {
            // 属性不存在，落到下方返回 nil
        } catch (IllegalAccessException e) {
            throw new ScriptException("Cannot read field '" + propertyName + "' on "
                + clazz.getSimpleName() + ": " + e.getMessage(), e);
        }

        return ScriptValue.nil();
    }

    @Override
    public void setProperty(Object target, String propertyName, ScriptValue value) {
        if (target == null) return;
        Class<?> clazz = target.getClass();

        String setterName = "set" + capitalize(propertyName);
        List<Method> candidates = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(setterName) && m.getParameterCount() == 1) {
                candidates.add(m);
            }
        }

        for (Method m : candidates) {
            Object converted;
            try {
                converted = convertArg(value, m.getParameterTypes()[0]);
            } catch (ArgConvertException e) {
                continue;  // 该重载参数类型不匹配，尝试下一个
            }
            invokeMethod(target, m, new Object[]{converted}, propertyName);
            return;
        }

        try {
            Field field = clazz.getField(propertyName);
            try {
                field.set(target, convertArg(value, field.getType()));
                return;
            } catch (ArgConvertException e) {
                throw new ScriptException("Cannot assign " + value + " to field '" + propertyName
                    + "' of type " + field.getType().getSimpleName() + " on " + clazz.getSimpleName());
            } catch (IllegalAccessException e) {
                throw new ScriptException("Cannot write field '" + propertyName + "' on "
                    + clazz.getSimpleName() + ": " + e.getMessage(), e);
            }
        } catch (NoSuchFieldException ignored) {
            // 无 setter 也无 public field，落到下方报错
        }

        if (candidates.isEmpty()) {
            throw new ScriptException("No setter '" + setterName + "' or public field '" + propertyName
                + "' on " + clazz.getSimpleName());
        }
        throw new ScriptException("Cannot convert " + value + " to any parameter type of '"
            + setterName + "' on " + clazz.getSimpleName());
    }

    @Override
    public ScriptValue callMethod(Object target, String methodName, ScriptValue... args) {
        if (target == null) return ScriptValue.nil();
        Class<?> clazz = target.getClass();

        List<Method> candidates = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName) && m.getParameterCount() == args.length) {
                candidates.add(m);
            }
        }
        if (candidates.isEmpty()) {
            throw new ScriptException("No method '" + methodName + "' with " + args.length
                + " argument(s) on " + clazz.getSimpleName());
        }

        for (Method m : candidates) {
            Class<?>[] paramTypes = m.getParameterTypes();
            Object[] converted = new Object[args.length];
            boolean convertible = true;
            for (int i = 0; i < args.length; i++) {
                try {
                    converted[i] = convertArg(args[i], paramTypes[i]);
                } catch (ArgConvertException e) {
                    convertible = false;
                    break;  // 该重载不匹配，尝试下一个
                }
            }
            if (!convertible) continue;

            // 方法已确定匹配，调用失败直接上抛，不再尝试其他重载
            Object result = invokeMethod(target, m, converted, methodName);
            if (m.getReturnType() == void.class) return ScriptValue.nil();
            return wrapResult(result);
        }

        throw new ScriptException("No overload of '" + methodName + "' on " + clazz.getSimpleName()
            + " accepts the given argument types");
    }

    /**
     * 统一的反射调用入口：转发真实异常，必要时用 setAccessible 重试一次
     * （Bukkit 的 CraftPlayer 等是非 public 实现类，public 方法也可能抛 IllegalAccessException）
     */
    private Object invokeMethod(Object target, Method method, Object[] args, String label) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException first) {
            try {
                method.setAccessible(true);
                return method.invoke(target, args);
            } catch (Exception retry) {
                throw new ScriptException("Cannot access '" + label + "' on "
                    + target.getClass().getSimpleName() + ": " + retry.getMessage(), retry);
            }
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            throw new ScriptException("Call to '" + label + "' on " + target.getClass().getSimpleName()
                + " failed: " + cause, cause);
        } catch (RuntimeException e) {
            throw new ScriptException("Call to '" + label + "' on " + target.getClass().getSimpleName()
                + " failed: " + e, e);
        }
    }

    /**
     * 查找 getter，仅认 Java Beans 规范的 getXxx() / isXxx()。
     * 有意不回退到裸方法名 clazz.getMethod(name)：那会把任意无参公共方法暴露成"属性"，
     * 读取 get("remove") 之类就会产生副作用。
     */
    private Method findGetter(Class<?> clazz, String propertyName) {
        return getterCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(propertyName, name -> {
                String cap = capitalize(name);
                try {
                    return Optional.of(clazz.getMethod("get" + cap));
                } catch (NoSuchMethodException ignored) {
                }
                try {
                    return Optional.of(clazz.getMethod("is" + cap));
                } catch (NoSuchMethodException ignored) {
                }
                return Optional.empty();
            })
            .orElse(null);
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private ScriptValue wrapResult(Object result) {
        return ScriptValue.of(result, this);
    }

    /**
     * 将脚本值转换为目标 Java 类型。
     * 无法转换时抛 {@link ArgConvertException}，由调用方跳到下一个重载候选，
     * 绝不返回 null —— 那会把 null 静默传进方法，重载误选时尤其危险。
     */
    private Object convertArg(ScriptValue value, Class<?> targetType) throws ArgConvertException {
        if (value.isNull()) {
            if (targetType.isPrimitive()) {
                throw new ArgConvertException();  // 基本类型不接受 null
            }
            return null;
        }
        // ObjectValue 必须先判：它的 asInt()/asBigDecimal() 会抛 ScriptException，
        // 若先走标量分支就会冲出重载候选循环，导致后面可匹配的重载被跳过
        if (value instanceof ScriptValue.ObjectValue) {
            Object raw = ((ScriptValue.ObjectValue) value).value();
            if (raw != null && targetType.isAssignableFrom(raw.getClass())) {
                return raw;
            }
            throw new ArgConvertException();
        }
        if (targetType == String.class) return value.asString();
        if (targetType == int.class || targetType == Integer.class) return value.asInt();
        if (targetType == long.class || targetType == Long.class) return value.asLong();
        if (targetType == double.class || targetType == Double.class) return value.asNumber();
        if (targetType == float.class || targetType == Float.class) return (float) value.asNumber();
        if (targetType == boolean.class || targetType == Boolean.class) return value.asBoolean();
        if (targetType == BigDecimal.class) return value.asBigDecimal();
        throw new ArgConvertException();
    }

    /** 参数转换失败的内部信号，仅用于在重载候选间跳转，不对外暴露 */
    private static final class ArgConvertException extends Exception {
        private ArgConvertException() {
            super(null, null, false, false);  // 无需栈追踪，纯控制流
        }
    }
}
