package crypticlib.util;

import crypticlib.CrypticLib;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射相关工具类
 */
public class ReflectionHelper {

    // getField(public含继承)与getDeclaredField(本类任意修饰符)语义不同,使用各自独立的缓存,避免互相污染
    // 缓存key使用Class对象而非类名字符串,避免不同ClassLoader中同名类互相命中
    private final static Map<Class<?>, Map<String, Field>> fieldCaches = new ConcurrentHashMap<>();
    private final static Map<Class<?>, Map<String, Field>> declaredFieldCaches = new ConcurrentHashMap<>();
    private final static Map<Class<?>, Object> singletonObjectMap = new ConcurrentHashMap<>();

    // ===== 方法缓存 =====
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>>> methodCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>>> declaredMethodCache = new ConcurrentHashMap<>();

    // ===== 构造器缓存 =====
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<ArgSig, Optional<Constructor<?>>>> constructorCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<ArgSig, Optional<Constructor<?>>>> declaredConstructorCache = new ConcurrentHashMap<>();

    // ===== 缓存清理 =====

    /**
     * 清理指定类的方法缓存
     */
    public static void clearMethodCache(@NotNull Class<?> clazz) {
        methodCache.remove(clazz);
        declaredMethodCache.remove(clazz);
    }

    /**
     * 清理指定类的构造器缓存
     */
    public static void clearConstructorCache(@NotNull Class<?> clazz) {
        constructorCache.remove(clazz);
        declaredConstructorCache.remove(clazz);
    }

    /**
     * 清理指定类的字段缓存
     */
    public static void clearFieldCache(@NotNull Class<?> clazz) {
        fieldCaches.remove(clazz);
        declaredFieldCaches.remove(clazz);
    }

    /**
     * 清理指定类的所有反射缓存
     */
    public static void clearCache(@NotNull Class<?> clazz) {
        clearMethodCache(clazz);
        clearConstructorCache(clazz);
        clearFieldCache(clazz);
        singletonObjectMap.remove(clazz);
    }

    /**
     * 清理所有反射缓存
     */
    public static void clearAllCaches() {
        methodCache.clear();
        declaredMethodCache.clear();
        constructorCache.clear();
        declaredConstructorCache.clear();
        fieldCaches.clear();
        declaredFieldCaches.clear();
        singletonObjectMap.clear();
    }

    // ===== 字段 =====

    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) throws NoSuchFieldException {
        Field cacheField = getFieldCache(fieldCaches, clazz, fieldName);
        if (cacheField != null)
            return cacheField;
        Field field = clazz.getField(fieldName);
        putFieldCache(fieldCaches, clazz, fieldName, field);
        return field;
    }

    public static Field getDeclaredField(@NotNull Class<?> clazz, @NotNull String fieldName) throws NoSuchFieldException {
        Field cacheField = getFieldCache(declaredFieldCaches, clazz, fieldName);
        if (cacheField != null)
            return cacheField;
        Field field = clazz.getDeclaredField(fieldName);
        putFieldCache(declaredFieldCaches, clazz, fieldName, field);
        return field;
    }

    private static Field getFieldCache(Map<Class<?>, Map<String, Field>> caches, Class<?> clazz, String fieldName) {
        Map<String, Field> classFieldCache = caches.get(clazz);
        return classFieldCache != null ? classFieldCache.get(fieldName) : null;
    }

    private static void putFieldCache(Map<Class<?>, Map<String, Field>> caches, Class<?> clazz, String fieldName, Field field) {
        caches.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>()).put(fieldName, field);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldObj(@NotNull Field field, @Nullable Object owner) throws IllegalAccessException {
        return (T) field.get(owner);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDeclaredFieldObj(@NotNull Field field, @Nullable Object owner) throws IllegalAccessException {
        field.setAccessible(true);
        return (T) field.get(owner);
    }

    public static <T> void setFieldObj(@NotNull Field field, @Nullable Object owner, @NotNull T value) throws IllegalAccessException {
        field.set(owner, value);
    }

    public static <T> void setDeclaredFieldObj(@NotNull Field field, @Nullable Object owner, @NotNull T value) throws IllegalAccessException {
        field.setAccessible(true);
        field.set(owner, value);
    }

    // ===== 方法 =====

    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... argClasses) throws NoSuchMethodException {
        ArgSig sig = new ArgSig(argClasses);
        Optional<Method> cached = getMethodFromCache(methodCache, clazz, methodName, sig);
        if (cached != null) {
            return cached.orElse(null);
        }
        Method found = clazz.getMethod(methodName, argClasses);
        putMethodCache(methodCache, clazz, methodName, sig, found);
        return found;
    }

    public static Method getDeclaredMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... argClasses) throws NoSuchMethodException {
        ArgSig sig = new ArgSig(argClasses);
        Optional<Method> cached = getMethodFromCache(declaredMethodCache, clazz, methodName, sig);
        if (cached != null) {
            return cached.orElse(null);
        }
        Method found = clazz.getDeclaredMethod(methodName, argClasses);
        putMethodCache(declaredMethodCache, clazz, methodName, sig, found);
        return found;
    }

    public static Object invokeMethod(@NotNull Method method, @Nullable Object invokeObj, Object... args) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        return method.invoke(invokeObj, args);
    }

    public static Object invokeDeclaredMethod(@NotNull Method method, @Nullable Object invokeObj, Object... args) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        return method.invoke(invokeObj, args);
    }

    @NotNull
    public static List<Method> collectCandidates(@NotNull Class<?> clazz, @NotNull String methodName, int paramCount) {
        List<Method> found = new ArrayList<>();
        for (Method m : clazz.getMethods()) {
            if (m.getName().equals(methodName) && m.getParameterCount() == paramCount) {
                found.add(m);
            }
        }
        return found;
    }

    // ===== 构造器 =====

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(@NotNull Class<T> clazz, Class<?>... argClasses) throws NoSuchMethodException {
        ArgSig sig = new ArgSig(argClasses);
        Optional<Constructor<?>> cached = getConstructorFromCache(constructorCache, clazz, sig);
        if (cached != null) {
            return (Constructor<T>) cached.orElse(null);
        }
        Constructor<T> found = clazz.getConstructor(argClasses);
        putConstructorCache(constructorCache, clazz, sig, found);
        return found;
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getDeclaredConstructor(@NotNull Class<T> clazz, Class<?>... argClasses) throws NoSuchMethodException {
        ArgSig sig = new ArgSig(argClasses);
        Optional<Constructor<?>> cached = getConstructorFromCache(declaredConstructorCache, clazz, sig);
        if (cached != null) {
            return (Constructor<T>) cached.orElse(null);
        }
        Constructor<T> found = clazz.getDeclaredConstructor(argClasses);
        putConstructorCache(declaredConstructorCache, clazz, sig, found);
        return found;
    }

    public static <T> T invokeConstructor(@NotNull Constructor<T> constructor, Object... args) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        return constructor.newInstance(args);
    }

    public static <T> T invokeDeclaredConstructor(@NotNull Constructor<T> constructor, Object... args) throws InstantiationException, IllegalAccessException, InvocationTargetException {
        constructor.setAccessible(true);
        return constructor.newInstance(args);
    }

    public static <T> T newInstance(Class<T> clazz, Object... args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        Constructor<T> constructor = getConstructor(clazz, argClasses);
        return invokeConstructor(constructor, args);
    }

    public static <T> T newDeclaredInstance(Class<T> clazz, Object... args) throws NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        Constructor<T> constructor = getDeclaredConstructor(clazz, argClasses);
        return invokeDeclaredConstructor(constructor, args);
    }

    // ===== 单例 =====

    @SuppressWarnings("unchecked")
    public static <T> T getSingletonClassInstance(Class<T> clazz, Object...objects) throws NoClassDefFoundError, ClassNotFoundException {
        if (CrypticLib.plugin().getClass().isAssignableFrom(clazz)) {
            return (T) CrypticLib.plugin();
        }
        return (T) singletonObjectMap.computeIfAbsent(clazz, k -> createSingleton(clazz, objects));
    }

    private static <T> T createSingleton(Class<T> clazz, Object... objects) {
        if (clazz.isEnum()) {
            return clazz.getEnumConstants()[0];
        }
        try {
            Field instanceField = ReflectionHelper.getDeclaredField(clazz, "INSTANCE");
            if (Modifier.isStatic(instanceField.getModifiers()) && instanceField.getType().equals(clazz)) {
                return getDeclaredFieldObj(instanceField, null);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        try {
            return ReflectionHelper.newDeclaredInstance(clazz, objects);
        } catch (NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    // ===== 通用缓存模板 =====

    private static Optional<Method> getMethodFromCache(
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>>> cache,
        Class<?> clazz, String methodName, ArgSig sig
    ) {
        ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>> nameMap = cache.get(clazz);
        if (nameMap == null) return null;
        ConcurrentHashMap<ArgSig, Optional<Method>> sigMap = nameMap.get(methodName);
        if (sigMap == null) return null;
        return sigMap.get(sig);
    }

    private static void putMethodCache(
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>>> cache,
        Class<?> clazz, String methodName, ArgSig sig, Method method
    ) {
        cache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(methodName, k -> new ConcurrentHashMap<>())
            .putIfAbsent(sig, Optional.ofNullable(method));
    }

    @SuppressWarnings("unchecked")
    private static Optional<Constructor<?>> getConstructorFromCache(
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<ArgSig, Optional<Constructor<?>>>> cache,
        Class<?> clazz, ArgSig sig
    ) {
        ConcurrentHashMap<ArgSig, Optional<Constructor<?>>> sigMap = cache.get(clazz);
        if (sigMap == null) return null;
        return sigMap.get(sig);
    }

    private static void putConstructorCache(
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<ArgSig, Optional<Constructor<?>>>> cache,
        Class<?> clazz, ArgSig sig, Constructor<?> constructor
    ) {
        cache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .putIfAbsent(sig, Optional.ofNullable(constructor));
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

}
