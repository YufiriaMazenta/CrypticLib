package crypticlib.script.object;

import crypticlib.script.ScriptException;
import crypticlib.script.ScriptValue;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
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
    private static final Object SENTINEL = new Object();

    /**
     * 清理所有反射缓存
     */
    public void clearAllCaches() {
        getterCache.clear();
        resolvedMethodCache.clear();
    }

    /**
     * 清理指定类的反射缓存
     */
    public void clearCache(Class<?> clazz) {
        getterCache.remove(clazz);
        resolvedMethodCache.remove(clazz);
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

        ResolvedCall setterCall = resolve(setterName, candidates, new ScriptValue[]{value}, clazz);
        if (setterCall == null) {
            //只有"按类型就不存在可用重载"才写负缓存: 枚举名非法/超出 byte 范围/字符长度不对这类
            //失败与具体取值有关, 一旦缓存成"该方法不存在", 后续传入合法值也会被直接拒绝
            if (!hasTypeMatch(candidates, new ScriptValue[]{value})) {
                putMethodCache(clazz, setterName, sig, null);
            }
            throw new ScriptException("Cannot convert " + value + " to any parameter type of '"
                + setterName + "' on " + clazz.getSimpleName());
        }
        putMethodCache(clazz, setterName, sig, setterCall.method);
        invokeMethod(target, setterCall.method, setterCall.converted, propertyName);
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

        ResolvedCall resolvedCall = resolve(methodName, candidates, args, clazz);
        if (resolvedCall == null) {
            //只有"按类型就不存在可用重载"才写负缓存, 取值相关的失败(枚举名、数值范围等)不缓存,
            //否则一次非法取值会永久封死后续所有合法调用
            if (!hasTypeMatch(candidates, args)) {
                putMethodCache(clazz, methodName, sig, null);
            }
            throw new ScriptException("No overload of '" + methodName + "' on " + clazz.getSimpleName()
                + " accepts the given argument types");
        }
        putMethodCache(clazz, methodName, sig, resolvedCall.method);
        Object result = invokeMethod(target, resolvedCall.method, resolvedCall.converted, methodName);
        if (resolvedCall.method.getReturnType() == void.class) return ScriptValue.nil();
        return wrapResult(result);
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
        List<Method> found = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            //桥接/合成方法会与真实方法产生同分候选, 造成误判歧义, 直接跳过
            if (m.isBridge() || m.isSynthetic()) continue;
            if (m.getName().equals(methodName) && m.getParameterCount() == paramCount) {
                found.add(m);
            }
        }
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
                // 1. getXxx()
                try {
                    return Optional.of(clazz.getMethod("get" + cap));
                } catch (NoSuchMethodException ignored) {
                }
                // 2. xxx() — 仅匹配有返回值的无参方法（record 访问器或自定义 getter 风格）
                try {
                    Method m = clazz.getMethod(name);
                    if (m.getParameterCount() == 0 && m.getReturnType() != void.class) {
                        return Optional.of(m);
                    }
                } catch (NoSuchMethodException ignored) {
                }
                // 3. isXxx()
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
        try {
            //枚举: 按常量名精确匹配, 再退回忽略大小写/首尾空白匹配
            if (targetType.isEnum()) {
                return convertToEnum(value, targetType);
            }
            //Object/Number/Comparable/CharSequence 这类宽泛形参必须能拿到可直接赋值的目标值,
            //否则像 List.add(E) / Map.put(K,V) 这种泛型擦除成 Object 的 API 会全部不可用
            if (targetType == Object.class) {
                return naturalValue(value);
            }
            if (targetType == Number.class
                || targetType == Comparable.class
                || targetType == CharSequence.class) {
                Object natural = naturalValue(value);
                if (natural != null && targetType.isAssignableFrom(natural.getClass())) {
                    return natural;
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
            if (targetType == short.class || targetType == Short.class) {
                long longValue = value.asLong();
                if (longValue < Short.MIN_VALUE || longValue > Short.MAX_VALUE) {
                    throw new ArgConvertException();
                }
                return (short) longValue;
            }
            if (targetType == byte.class || targetType == Byte.class) {
                long longValue = value.asLong();
                if (longValue < Byte.MIN_VALUE || longValue > Byte.MAX_VALUE) {
                    throw new ArgConvertException();
                }
                return (byte) longValue;
            }
            if (targetType == char.class || targetType == Character.class) {
                String text = value.asString();
                if (text.length() != 1) {
                    throw new ArgConvertException();
                }
                return text.charAt(0);
            }
        } catch (ScriptException e) {
            //脚本层类型无法转换: 视为"这个重载不匹配", 让重载解析继续尝试其它候选
            throw new ArgConvertException();
        }
        throw new ArgConvertException();
    }

    /**
     * 把脚本值转成最自然的 Java 值, 用于 Object/Number/Comparable 这类宽泛形参
     */
    private static Object naturalValue(ScriptValue value) {
        if (value instanceof ScriptValue.Str) {
            return ((ScriptValue.Str) value).value();
        }
        if (value instanceof ScriptValue.Int) {
            ScriptValue.Int intValue = (ScriptValue.Int) value;
            return intValue.valueType() == long.class ? (Object) intValue.value() : (Object) (int) intValue.value();
        }
        if (value instanceof ScriptValue.Num) {
            return ((ScriptValue.Num) value).value();
        }
        if (value instanceof ScriptValue.Bool) {
            return ((ScriptValue.Bool) value).value();
        }
        if (value instanceof ScriptValue.ObjectValue) {
            return ((ScriptValue.ObjectValue) value).value();
        }
        return null;
    }

    /**
     * 把脚本值转成枚举常量
     */
    private static Object convertToEnum(ScriptValue value, Class<?> enumType) throws ArgConvertException {
        Object[] constants = enumType.getEnumConstants();
        if (constants == null || constants.length == 0) {
            throw new ArgConvertException();
        }
        String raw = value.asString();
        for (Object constant : constants) {
            if (((Enum<?>) constant).name().equals(raw)) {
                return constant;
            }
        }
        String normalized = raw.trim();
        for (Object constant : constants) {
            if (((Enum<?>) constant).name().equalsIgnoreCase(normalized)) {
                return constant;
            }
        }
        throw new ArgConvertException();
    }

    /**
     * 计算某个实参对某个形参的匹配优先级, 数值越小越优先, 负数表示不匹配。
     * <p>
     * 该分数只用于给同一方法名的多个重载排序: 精确匹配优先, 其次是放宽转换,
     * 最后才是字符串/数字/布尔之间的有损转换。真正能否转换仍由 {@link #convertArg} 决定。
     */
    private static int rank(ScriptValue value, Class<?> targetType) {
        if (value instanceof ScriptValue.ObjectValue) {
            Object raw = ((ScriptValue.ObjectValue) value).value();
            if (raw == null) {
                return targetType.isPrimitive() ? -1 : 20;
            }
            if (!targetType.isAssignableFrom(raw.getClass())) {
                return -1;
            }
            return targetType == raw.getClass() ? 0 : 1;
        }
        if (value.isNull()) {
            return targetType.isPrimitive() ? -1 : 20;
        }
        if (targetType.isEnum()) {
            return 6;
        }
        if (targetType == boolean.class || targetType == Boolean.class) {
            return value instanceof ScriptValue.Bool ? 0 : 40;
        }
        if (targetType == char.class || targetType == Character.class) {
            if (!(value instanceof ScriptValue.Str)) {
                return -1;
            }
            return ((ScriptValue.Str) value).value().length() == 1 ? 7 : -1;
        }
        if (value instanceof ScriptValue.Str) {
            if (targetType == String.class) return 0;
            if (targetType == CharSequence.class) return 2;
            if (targetType == Object.class || targetType == Comparable.class) return 3;
            if (isNumericType(targetType)) return 15;
            return -1;
        }
        if (value instanceof ScriptValue.Bool) {
            if (targetType == Object.class || targetType == Comparable.class) return 3;
            if (targetType == String.class) return 12;
            if (targetType == CharSequence.class) return 13;
            if (isNumericType(targetType)) return 40;
            return -1;
        }
        if (value instanceof ScriptValue.Int) {
            boolean isIntValue = ((ScriptValue.Int) value).valueType() == int.class;
            if (targetType == int.class || targetType == Integer.class) return isIntValue ? 0 : 2;
            if (targetType == long.class || targetType == Long.class) return isIntValue ? 2 : 0;
            if (targetType == double.class || targetType == Double.class) return 4;
            if (targetType == float.class || targetType == Float.class) return 5;
            if (targetType == BigDecimal.class) return 8;
            if (targetType == short.class || targetType == Short.class) return 6;
            if (targetType == byte.class || targetType == Byte.class) return 7;
            if (targetType == Object.class || targetType == Number.class || targetType == Comparable.class) return 3;
            if (targetType == String.class) return 12;
            if (targetType == CharSequence.class) return 13;
            return -1;
        }
        if (value instanceof ScriptValue.Num) {
            if (targetType == BigDecimal.class) return 0;
            if (targetType == double.class || targetType == Double.class) return 1;
            if (targetType == float.class || targetType == Float.class) return 2;
            if (targetType == long.class || targetType == Long.class) return 4;
            if (targetType == int.class || targetType == Integer.class) return 5;
            if (targetType == short.class || targetType == Short.class) return 6;
            if (targetType == byte.class || targetType == Byte.class) return 7;
            if (targetType == Object.class || targetType == Number.class || targetType == Comparable.class) return 3;
            if (targetType == String.class) return 12;
            if (targetType == CharSequence.class) return 13;
            return -1;
        }
        return -1;
    }

    private static boolean isNumericType(Class<?> type) {
        return type == int.class || type == Integer.class
            || type == long.class || type == Long.class
            || type == short.class || type == Short.class
            || type == byte.class || type == Byte.class
            || type == double.class || type == Double.class
            || type == float.class || type == Float.class
            || type == BigDecimal.class;
    }

    /**
     * 在多个同分候选中判断 a 是否比 b 更具体(Java 的重载消歧规则)
     */
    private static boolean isMoreSpecific(Method a, Method b) {
        Class<?>[] aTypes = a.getParameterTypes();
        Class<?>[] bTypes = b.getParameterTypes();
        if (aTypes.length != bTypes.length) {
            return false;
        }
        boolean strictlyMoreSpecific = false;
        for (int i = 0; i < aTypes.length; i++) {
            if (aTypes[i].equals(bTypes[i])) {
                continue;
            }
            if (!bTypes[i].isAssignableFrom(aTypes[i])) {
                return false;
            }
            strictlyMoreSpecific = true;
        }
        return strictlyMoreSpecific;
    }

    /**
     * 解析出唯一匹配的重载并完成参数转换。
     *
     * @return 唯一匹配的调用; 无候选或候选全部无法转换时返回 null
     * @throws ScriptException 存在多个无法消歧的候选时抛出
     */
    private ResolvedCall resolve(String methodName, List<Method> candidates, ScriptValue[] args, Class<?> clazz) {
        List<ResolvedCall> matches = new ArrayList<>();
        for (Method method : candidates) {
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] converted = new Object[args.length];
            int score = 0;
            boolean matched = true;
            for (int i = 0; i < args.length; i++) {
                int rank = rank(args[i], paramTypes[i]);
                if (rank < 0) {
                    matched = false;
                    break;
                }
                score += rank;
                try {
                    converted[i] = convertArg(args[i], paramTypes[i]);
                } catch (ArgConvertException e) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                matches.add(new ResolvedCall(method, converted, score));
            }
        }
        if (matches.isEmpty()) {
            return null;
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }
        matches.sort(Comparator.comparingInt(call -> call.score));
        ResolvedCall best = matches.get(0);
        if (best.score != matches.get(1).score) {
            return best;
        }
        List<ResolvedCall> tied = new ArrayList<>();
        for (ResolvedCall call : matches) {
            if (call.score == best.score) {
                tied.add(call);
            }
        }
        for (ResolvedCall candidate : tied) {
            boolean mostSpecific = true;
            for (ResolvedCall other : tied) {
                if (candidate == other) {
                    continue;
                }
                if (!isMoreSpecific(candidate.method, other.method)) {
                    mostSpecific = false;
                    break;
                }
            }
            if (mostSpecific) {
                return candidate;
            }
        }
        throw new ScriptException("Ambiguous method '" + methodName + "' on " + clazz.getSimpleName()
            + ", candidates: " + describeCandidates(tied));
    }

    /**
     * 判断是否存在"仅按类型就能匹配"的候选(不考虑具体取值能否转换)。
     * <p>
     * 用于区分两类失败: 类型上就不存在可用重载(可安全负缓存),
     * 与类型匹配但取值无法转换(枚举名非法、超出数值范围等, 不可负缓存)。
     */
    private static boolean hasTypeMatch(List<Method> candidates, ScriptValue[] args) {
        for (Method method : candidates) {
            Class<?>[] paramTypes = method.getParameterTypes();
            boolean matched = true;
            for (int i = 0; i < args.length; i++) {
                if (rank(args[i], paramTypes[i]) < 0) {
                    matched = false;
                    break;
                }
            }
            if (matched) {
                return true;
            }
        }
        return false;
    }

    private static String describeCandidates(List<ResolvedCall> candidates) {        List<String> names = new ArrayList<>();
        for (ResolvedCall candidate : candidates) {
            names.add(candidate.method.getName() + Arrays.toString(candidate.method.getParameterTypes()));
        }
        return String.join(", ", names);
    }

    /**
     * 已完成重载解析与参数转换的一次调用
     */
    private static final class ResolvedCall {
        private final Method method;
        private final Object[] converted;
        private final int score;

        private ResolvedCall(Method method, Object[] converted, int score) {
            this.method = method;
            this.converted = converted;
            this.score = score;
        }
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
