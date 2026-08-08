package crypticlib.script.object;

import crypticlib.script.ScriptException;
import crypticlib.script.ScriptValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
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
public enum ReflectPropertyResolver implements PropertyResolver {

    INSTANCE;

    /** 类 → (属性名 → getter)，Optional.empty() 用于负向缓存，避免缺失属性每次重跑反射 */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Optional<Method>>> getterCache = new ConcurrentHashMap<>();
    /**
     * 方法解析缓存（setter 与 callMethod 共用）。
     * 结构：Class → (方法名 → (参数类型签名 → Optional.of(Method) | Optional.empty()))
     * <p>
     * 查找策略：
     * 1. 按实际参数类型精确查找 → O(1)，命中即返回
     * 2. 未命中 → 遍历同名同参数个数的候选方法，尝试 convertArg 类型转换
     * 3. 转换成功则缓存该类型签名 → Optional.of(method)；全部失败则缓存 Optional.empty()
     * 4. 后续相同类型签名直接命中缓存，不再遍历
     */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>>> resolvedMethodCache = new ConcurrentHashMap<>();
    /** fastExtract 的类型不匹配哨兵 */
    private static final Object SENTINEL = new Object();

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

        // 1. 先按实际参数类型查缓存（精确命中 O(1)）
        Method resolved = resolvedMethodCacheHit(clazz, setterName, value);
        if (resolved != null) {
            Object arg = fastExtract(value, resolved.getParameterTypes()[0]);
            if (arg != SENTINEL) {
                invokeMethod(target, resolved, new Object[]{arg}, propertyName);
                return;
            }
            // 类型不匹配（罕见：缓存后 ScriptValue 子类变化），走 convertArg
            try {
                invokeMethod(target, resolved, new Object[]{convertArg(value, resolved.getParameterTypes()[0])}, propertyName);
                return;
            } catch (ArgConvertException e) {
                throw new ScriptException("Cannot convert " + value + " to parameter type of '"
                    + setterName + "' on " + clazz.getSimpleName());
            }
        }
        if (resolvedMethodCacheMiss(clazz, setterName, value)) {
            // 已缓存但无匹配 → 直接走 field fallback 或报错
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
            }
            throw new ScriptException("Cannot convert " + value + " to any parameter type of '"
                + setterName + "' on " + clazz.getSimpleName());
        }

        // 2. 缓存未命中 → 收集候选 + 转换匹配 + 缓存结果
        List<Method> candidates = collectCandidates(clazz, setterName, 1);
        if (candidates.isEmpty()) {
            // 无 setter，尝试 public field
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
            }
            throw new ScriptException("No setter '" + setterName + "' or public field '" + propertyName
                + "' on " + clazz.getSimpleName());
        }

        // 有候选但缓存中没有精确命中 → 逐个尝试类型转换
        for (Method m : candidates) {
            Object converted;
            try {
                converted = convertArg(value, m.getParameterTypes()[0]);
            } catch (ArgConvertException e) {
                continue;
            }
            cacheResolved(clazz, setterName, new Class<?>[]{value.actualType()}, m);
            invokeMethod(target, m, new Object[]{converted}, propertyName);
            return;
        }

        cacheResolved(clazz, setterName, new Class<?>[]{value.actualType()}, null);
        throw new ScriptException("Cannot convert " + value + " to any parameter type of '"
            + setterName + "' on " + clazz.getSimpleName());
    }

    @Override
    public ScriptValue callMethod(Object target, String methodName, ScriptValue... args) {
        if (target == null) return ScriptValue.nil();
        Class<?> clazz = target.getClass();

        // 1. 先按实际参数类型查缓存（精确命中 O(1)）
        Method resolved = resolvedMethodCacheHit(clazz, methodName, args);
        if (resolved != null) {
            Class<?>[] paramTypes = resolved.getParameterTypes();
            Object[] converted = new Object[args.length];
            boolean allFast = true;
            for (int i = 0; i < args.length; i++) {
                Object v = fastExtract(args[i], paramTypes[i]);
                if (v == SENTINEL) { allFast = false; break; }
                converted[i] = v;
            }
            if (allFast) {
                Object result = invokeMethod(target, resolved, converted, methodName);
                if (resolved.getReturnType() == void.class) return ScriptValue.nil();
                return wrapResult(result);
            }
            // 罕见 fallback
            try {
                for (int i = 0; i < args.length; i++) {
                    if (converted[i] == null || converted[i] == SENTINEL) {
                        converted[i] = convertArg(args[i], paramTypes[i]);
                    }
                }
            } catch (ArgConvertException e) {
                throw new ScriptException("Cannot convert argument for '" + methodName + "' on " + clazz.getSimpleName());
            }
            Object result = invokeMethod(target, resolved, converted, methodName);
            if (resolved.getReturnType() == void.class) return ScriptValue.nil();
            return wrapResult(result);
        }
        if (resolvedMethodCacheMiss(clazz, methodName, args)) {
            // 已缓存但无匹配 → 直接报错
            throw new ScriptException("No overload of '" + methodName + "' on " + clazz.getSimpleName()
                + " accepts the given argument types");
        }

        // 2. 缓存未命中 → 收集候选 + 转换匹配 + 缓存结果
        List<Method> candidates = collectCandidates(clazz, methodName, args.length);
        if (candidates.isEmpty()) {
            throw new ScriptException("No method '" + methodName + "' with " + args.length
                + " argument(s) on " + clazz.getSimpleName());
        }

        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].actualType();
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
                    break;
                }
            }
            if (!convertible) continue;

            cacheResolved(clazz, methodName, argTypes, m);
            Object result = invokeMethod(target, m, converted, methodName);
            if (m.getReturnType() == void.class) return ScriptValue.nil();
            return wrapResult(result);
        }

        cacheResolved(clazz, methodName, argTypes, null);
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

    /**
     * 缓存命中且有匹配 → 返回 Method；否则返回 null（需进一步判断 miss 或未缓存）
     */
    private Method resolvedMethodCacheHit(Class<?> clazz, String methodName, ScriptValue... args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].actualType();
        }
        ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>> nameMap = resolvedMethodCache.get(clazz);
        if (nameMap == null) return null;
        ConcurrentHashMap<ArgSig, Optional<Method>> sigMap = nameMap.get(methodName);
        if (sigMap == null) return null;
        Optional<Method> opt = sigMap.get(new ArgSig(argTypes));
        return opt != null && opt.isPresent() ? opt.get() : null;
    }

    /**
     * 缓存命中但无匹配 → 返回 true；未缓存 → 返回 false
     */
    private boolean resolvedMethodCacheMiss(Class<?> clazz, String methodName, ScriptValue... args) {
        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].actualType();
        }
        ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>> nameMap = resolvedMethodCache.get(clazz);
        if (nameMap == null) return false;
        ConcurrentHashMap<ArgSig, Optional<Method>> sigMap = nameMap.get(methodName);
        if (sigMap == null) return false;
        Optional<Method> opt = sigMap.get(new ArgSig(argTypes));
        return opt != null && !opt.isPresent();
    }

    /**
     * 轻量值提取：缓存命中时跳过 convertArg 的类型检查链，直接按已知类型取值。
     * 类型不匹配返回 {@link #SENTINEL}（应 fallback 到 convertArg）。
     */
    private Object fastExtract(ScriptValue value, Class<?> targetType) {
        if (value instanceof ScriptValue.Str && targetType == String.class) return ((ScriptValue.Str) value).value();
        if (value instanceof ScriptValue.Int) {
            if (targetType == long.class || targetType == Long.class) return ((ScriptValue.Int) value).value();
            if (targetType == int.class || targetType == Integer.class) return (int) ((ScriptValue.Int) value).value();
        }
        if (value instanceof ScriptValue.Num) {
            if (targetType == BigDecimal.class) return ((ScriptValue.Num) value).value();
            if (targetType == double.class || targetType == Double.class) return ((ScriptValue.Num) value).value().doubleValue();
            if (targetType == float.class || targetType == Float.class) return ((ScriptValue.Num) value).value().floatValue();
        }
        if (value instanceof ScriptValue.Bool && (targetType == boolean.class || targetType == Boolean.class))
            return ((ScriptValue.Bool) value).value();
        if (value instanceof ScriptValue.ObjectValue) {
            Object raw = ((ScriptValue.ObjectValue) value).value();
            if (raw != null && targetType.isAssignableFrom(raw.getClass())) return raw;
        }
        if (value.isNull() && !targetType.isPrimitive()) return null;
        return SENTINEL;
    }

    /** 缓存解析结果（包括负向结果 Optional.empty()） */
    private void cacheResolved(Class<?> clazz, String methodName, Class<?>[] argTypes, Method method) {
        resolvedMethodCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(methodName, k -> new ConcurrentHashMap<>())
            .putIfAbsent(new ArgSig(argTypes), Optional.ofNullable(method));
    }

    /**
     * 收集同名同参数个数的候选方法。
     * 首次调用时扫描 clazz.getMethods() 并缓存候选列表，后续直接命中。
     */
    private List<Method> collectCandidates(Class<?> clazz, String methodName, int paramCount) {
        String cacheKey = methodName + "#" + paramCount;
        ConcurrentHashMap<String, List<Method>> candMap = candidatesForClass(clazz);
        List<Method> result = candMap.get(cacheKey);
        if (result != null) return result;

        List<Method> found = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName) && m.getParameterCount() == paramCount) {
                found.add(m);
            }
        }
        candMap.put(cacheKey, found);
        return found;
    }

    /** 类 → (name#paramCount → 候选方法列表) 的独立缓存，与 resolvedMethodCache 分开 */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, List<Method>>> candidatesCache = new ConcurrentHashMap<>();

    private ConcurrentHashMap<String, List<Method>> candidatesForClass(Class<?> clazz) {
        return candidatesCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
    }

    /**
     * 参数类型签名，用作 resolvedMethodCache 的 key。
     * 支持精确类型查找和 hashCode/equals 语义。
     */
    private static final class ArgSig {
        private final Class<?>[] types;
        private final int hash;

        ArgSig(Class<?>[] types) {
            this.types = types;
            this.hash = Arrays.hashCode(types);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ArgSig)) return false;
            return Arrays.equals(types, ((ArgSig) o).types);
        }

        @Override
        public int hashCode() {
            return hash;
        }
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
