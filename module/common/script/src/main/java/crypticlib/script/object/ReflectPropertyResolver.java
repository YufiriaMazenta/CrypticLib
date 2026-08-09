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

    /** 类 → (属性名 → getter)，Optional.empty() 用于负向缓存 */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, Optional<Method>>> getterCache = new ConcurrentHashMap<>();
    /** 类 → (方法名 → (参数签名 → Method))，setter 与 callMethod 共用 */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>>> resolvedMethodCache = new ConcurrentHashMap<>();
    /** 类 → (name#paramCount → 候选方法列表) */
    private final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, List<Method>>> candidatesCache = new ConcurrentHashMap<>();

    private static final Object SENTINEL = new Object();

    /**
     * 清理所有反射缓存
     */
    public void clearAllCaches() {
        getterCache.clear();
        resolvedMethodCache.clear();
        candidatesCache.clear();
    }

    /**
     * 清理指定类的反射缓存
     */
    public void clearCache(Class<?> clazz) {
        getterCache.remove(clazz);
        resolvedMethodCache.remove(clazz);
        candidatesCache.remove(clazz);
    }

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
        ArgSig sig = new ArgSig(new Class<?>[]{value.actualType()});
        Optional<Method> cached = getMethodFromCache(clazz, setterName, sig);
        if (cached != null) {
            Method resolved = cached.orElse(null);
            if (resolved != null) {
                Object arg = fastExtract(value, resolved.getParameterTypes()[0]);
                if (arg != SENTINEL) {
                    invokeMethod(target, resolved, new Object[]{arg}, propertyName);
                    return;
                }
                try {
                    invokeMethod(target, resolved, new Object[]{convertArg(value, resolved.getParameterTypes()[0])}, propertyName);
                    return;
                } catch (ArgConvertException e) {
                    throw new ScriptException("Cannot convert " + value + " to parameter type of '"
                        + setterName + "' on " + clazz.getSimpleName());
                }
            }
            // 负向缓存命中 → field fallback
            setField(target, clazz, propertyName, value, setterName);
            return;
        }

        // 2. 缓存未命中 → 收集候选 + 转换匹配
        List<Method> candidates = collectCandidates(clazz, setterName, 1);
        if (candidates.isEmpty()) {
            putMethodCache(clazz, setterName, sig, null);
            setField(target, clazz, propertyName, value, setterName);
            return;
        }

        for (Method m : candidates) {
            Object converted;
            try {
                converted = convertArg(value, m.getParameterTypes()[0]);
            } catch (ArgConvertException e) {
                continue;
            }
            putMethodCache(clazz, setterName, sig, m);
            invokeMethod(target, m, new Object[]{converted}, propertyName);
            return;
        }

        putMethodCache(clazz, setterName, sig, null);
        throw new ScriptException("Cannot convert " + value + " to any parameter type of '"
            + setterName + "' on " + clazz.getSimpleName());
    }

    @Override
    public ScriptValue callMethod(Object target, String methodName, ScriptValue... args) {
        if (target == null) return ScriptValue.nil();
        Class<?> clazz = target.getClass();

        Class<?>[] argTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            argTypes[i] = args[i].actualType();
        }
        ArgSig sig = new ArgSig(argTypes);

        // 1. 先查缓存
        Optional<Method> cached = getMethodFromCache(clazz, methodName, sig);
        if (cached != null) {
            Method resolved = cached.orElse(null);
            if (resolved == null) {
                throw new ScriptException("No overload of '" + methodName + "' on " + clazz.getSimpleName()
                    + " accepts the given argument types");
            }
            return invokeResolved(target, resolved, args, methodName);
        }

        // 2. 缓存未命中 → 收集候选 + 转换匹配
        List<Method> candidates = collectCandidates(clazz, methodName, args.length);
        if (candidates.isEmpty()) {
            putMethodCache(clazz, methodName, sig, null);
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
                    break;
                }
            }
            if (!convertible) continue;

            putMethodCache(clazz, methodName, sig, m);
            Object result = invokeMethod(target, m, converted, methodName);
            if (m.getReturnType() == void.class) return ScriptValue.nil();
            return wrapResult(result);
        }

        putMethodCache(clazz, methodName, sig, null);
        throw new ScriptException("No overload of '" + methodName + "' on " + clazz.getSimpleName()
            + " accepts the given argument types");
    }

    // ===== 缓存操作 =====

    private Optional<Method> getMethodFromCache(Class<?> clazz, String methodName, ArgSig sig) {
        ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>> nameMap = resolvedMethodCache.get(clazz);
        if (nameMap == null) return null;
        ConcurrentHashMap<ArgSig, Optional<Method>> sigMap = nameMap.get(methodName);
        if (sigMap == null) return null;
        return sigMap.get(sig);
    }

    private void putMethodCache(Class<?> clazz, String methodName, ArgSig sig, Method method) {
        resolvedMethodCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(methodName, k -> new ConcurrentHashMap<>())
            .putIfAbsent(sig, Optional.ofNullable(method));
    }

    private List<Method> collectCandidates(Class<?> clazz, String methodName, int paramCount) {
        String cacheKey = methodName + "#" + paramCount;
        ConcurrentHashMap<String, List<Method>> candMap = candidatesCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>());
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

    // ===== 辅助方法 =====

    private ScriptValue invokeResolved(Object target, Method method, ScriptValue[] args, String methodName) {
        Class<?>[] paramTypes = method.getParameterTypes();
        Object[] converted = new Object[args.length];
        boolean allFast = true;
        for (int i = 0; i < args.length; i++) {
            Object v = fastExtract(args[i], paramTypes[i]);
            if (v == SENTINEL) { allFast = false; break; }
            converted[i] = v;
        }
        if (!allFast) {
            try {
                for (int i = 0; i < args.length; i++) {
                    if (converted[i] == null || converted[i] == SENTINEL) {
                        converted[i] = convertArg(args[i], paramTypes[i]);
                    }
                }
            } catch (ArgConvertException e) {
                throw new ScriptException("Cannot convert argument for '" + methodName + "' on " + target.getClass().getSimpleName());
            }
        }
        Object result = invokeMethod(target, method, converted, methodName);
        if (method.getReturnType() == void.class) return ScriptValue.nil();
        return wrapResult(result);
    }

    private void setField(Object target, Class<?> clazz, String propertyName, ScriptValue value, String setterName) {
        try {
            Field field = clazz.getField(propertyName);
            try {
                field.set(target, convertArg(value, field.getType()));
            } catch (ArgConvertException e) {
                throw new ScriptException("Cannot assign " + value + " to field '" + propertyName
                    + "' of type " + field.getType().getSimpleName() + " on " + clazz.getSimpleName());
            } catch (IllegalAccessException e) {
                throw new ScriptException("Cannot write field '" + propertyName + "' on "
                    + clazz.getSimpleName() + ": " + e.getMessage(), e);
            }
        } catch (NoSuchFieldException ignored) {
            throw new ScriptException("No setter '" + setterName + "' or public field '"
                + propertyName + "' on " + clazz.getSimpleName());
        }
    }

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

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private ScriptValue wrapResult(Object result) {
        return ScriptValue.of(result, this);
    }

    private Object convertArg(ScriptValue value, Class<?> targetType) throws ArgConvertException {
        if (value.isNull()) {
            if (targetType.isPrimitive()) {
                throw new ArgConvertException();
            }
            return null;
        }
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

    /**
     * 参数类型签名，用作缓存 key。
     * 构造时做防御性拷贝，隔离外部修改。
     */
    private static final class ArgSig {
        private final Class<?>[] types;
        private final int hash;

        ArgSig(Class<?>[] types) {
            this.types = types.clone();
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

    private static final class ArgConvertException extends Exception {
        private ArgConvertException() {
            super(null, null, false, false);
        }
    }

}
