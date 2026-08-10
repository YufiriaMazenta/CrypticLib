package crypticlib.util;

import crypticlib.CrypticLib;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射相关工具类
 */
public class ReflectionHelper {

    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // 字段缓存
    private final static Map<Class<?>, Map<String, FieldEntry>> fieldCaches = new ConcurrentHashMap<>();
    private final static Map<Class<?>, Map<String, FieldEntry>> declaredFieldCaches = new ConcurrentHashMap<>();
    private final static Map<Class<?>, Object> singletonObjectMap = new ConcurrentHashMap<>();

    // 方法缓存
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, MethodHandle>>> methodCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, MethodHandle>>> declaredMethodCache = new ConcurrentHashMap<>();

    // 构造器缓存
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<ArgSig, MethodHandle>> constructorCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Class<?>, ConcurrentHashMap<ArgSig, MethodHandle>> declaredConstructorCache = new ConcurrentHashMap<>();

    // ===== 缓存清理 =====

    public static void clearMethodCache(@NotNull Class<?> clazz) {
        methodCache.remove(clazz);
        declaredMethodCache.remove(clazz);
    }

    public static void clearConstructorCache(@NotNull Class<?> clazz) {
        constructorCache.remove(clazz);
        declaredConstructorCache.remove(clazz);
    }

    public static void clearFieldCache(@NotNull Class<?> clazz) {
        fieldCaches.remove(clazz);
        declaredFieldCaches.remove(clazz);
    }

    public static void clearCache(@NotNull Class<?> clazz) {
        clearMethodCache(clazz);
        clearConstructorCache(clazz);
        clearFieldCache(clazz);
        singletonObjectMap.remove(clazz);
    }

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
        FieldEntry entry = getFieldEntry(fieldCaches, clazz, fieldName);
        if (entry != null) return entry.field;
        Field field = clazz.getField(fieldName);
        putFieldEntry(fieldCaches, field.getDeclaringClass(), fieldName, field);
        return field;
    }

    public static Field getDeclaredField(@NotNull Class<?> clazz, @NotNull String fieldName) throws NoSuchFieldException {
        FieldEntry entry = getFieldEntry(declaredFieldCaches, clazz, fieldName);
        if (entry != null) return entry.field;
        Field field = clazz.getDeclaredField(fieldName);
        putFieldEntry(declaredFieldCaches, clazz, fieldName, field);
        return field;
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldObj(@NotNull Field field, @Nullable Object owner) throws IllegalAccessException {
        FieldEntry entry = findFieldEntry(fieldCaches, field);
        if (entry != null && entry.getter != null) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    return (T) entry.getter.invoke();
                } else {
                    return (T) entry.getter.invoke(owner);
                }
            } catch (Throwable e) {
                if (e instanceof IllegalAccessException) throw (IllegalAccessException) e;
                throw new RuntimeException(e);
            }
        }
        return (T) field.get(owner);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDeclaredFieldObj(@NotNull Field field, @Nullable Object owner) throws IllegalAccessException {
        FieldEntry entry = findFieldEntry(declaredFieldCaches, field);
        if (entry != null && entry.getter != null) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    return (T) entry.getter.invoke();
                } else {
                    return (T) entry.getter.invoke(owner);
                }
            } catch (Throwable e) {
                if (e instanceof IllegalAccessException) throw (IllegalAccessException) e;
                throw new RuntimeException(e);
            }
        }
        field.setAccessible(true);
        return (T) field.get(owner);
    }

    public static <T> void setFieldObj(@NotNull Field field, @Nullable Object owner, @NotNull T value) throws IllegalAccessException {
        FieldEntry entry = findFieldEntry(fieldCaches, field);
        if (entry != null && entry.setter != null) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    entry.setter.invoke(value);
                } else {
                    entry.setter.invoke(owner, value);
                }
                return;
            } catch (Throwable e) {
                if (e instanceof IllegalAccessException) throw (IllegalAccessException) e;
                throw new RuntimeException(e);
            }
        }
        field.set(owner, value);
    }

    public static <T> void setDeclaredFieldObj(@NotNull Field field, @Nullable Object owner, @NotNull T value) throws IllegalAccessException {
        FieldEntry entry = findFieldEntry(declaredFieldCaches, field);
        if (entry != null && entry.setter != null) {
            try {
                if (Modifier.isStatic(field.getModifiers())) {
                    entry.setter.invoke(value);
                } else {
                    entry.setter.invoke(owner, value);
                }
                return;
            } catch (Throwable e) {
                if (e instanceof IllegalAccessException) throw (IllegalAccessException) e;
                throw new RuntimeException(e);
            }
        }
        field.setAccessible(true);
        field.set(owner, value);
    }

    private static FieldEntry getFieldEntry(Map<Class<?>, Map<String, FieldEntry>> caches, Class<?> clazz, String fieldName) {
        Map<String, FieldEntry> classCache = caches.get(clazz);
        return classCache != null ? classCache.get(fieldName) : null;
    }

    private static FieldEntry findFieldEntry(Map<Class<?>, Map<String, FieldEntry>> caches, Field field) {
        Map<String, FieldEntry> classCache = caches.get(field.getDeclaringClass());
        return classCache != null ? classCache.get(field.getName()) : null;
    }

    private static void putFieldEntry(Map<Class<?>, Map<String, FieldEntry>> caches, Class<?> clazz, String fieldName, Field field) {
        try {
            field.setAccessible(true);
            MethodHandle getter = LOOKUP.unreflectGetter(field);
            MethodHandle setter = LOOKUP.unreflectSetter(field);
            caches.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
                .put(fieldName, new FieldEntry(field, getter, setter));
        } catch (IllegalAccessException e) {
            // 回退：只存 Field，不存 MethodHandle
            caches.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
                .put(fieldName, new FieldEntry(field, null, null));
        }
    }

    // ===== 方法 =====

    public static MethodHandle getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... argClasses) throws NoSuchMethodException, IllegalAccessException {
        ArgSig sig = new ArgSig(argClasses);
        MethodHandle cached = getMethodFromCache(methodCache, clazz, methodName, sig);
        if (cached != null) return cached;
        Method method = clazz.getMethod(methodName, argClasses);
        method.setAccessible(true);
        MethodHandle handle = LOOKUP.unreflect(method);
        putMethodCache(methodCache, clazz, methodName, sig, handle);
        return handle;
    }

    public static MethodHandle getDeclaredMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... argClasses) throws NoSuchMethodException, IllegalAccessException {
        ArgSig sig = new ArgSig(argClasses);
        MethodHandle cached = getMethodFromCache(declaredMethodCache, clazz, methodName, sig);
        if (cached != null) return cached;
        Method method = clazz.getDeclaredMethod(methodName, argClasses);
        method.setAccessible(true);
        MethodHandle handle = LOOKUP.unreflect(method);
        putMethodCache(declaredMethodCache, clazz, methodName, sig, handle);
        return handle;
    }

    // ===== 构造器 =====

    public static MethodHandle getConstructor(@NotNull Class<?> clazz, Class<?>... argClasses) throws NoSuchMethodException, IllegalAccessException {
        ArgSig sig = new ArgSig(argClasses);
        MethodHandle cached = getConstructorFromCache(constructorCache, clazz, sig);
        if (cached != null) return cached;
        Constructor<?> constructor = clazz.getConstructor(argClasses);
        constructor.setAccessible(true);
        MethodHandle handle = LOOKUP.unreflectConstructor(constructor);
        putConstructorCache(constructorCache, clazz, sig, handle);
        return handle;
    }

    public static MethodHandle getDeclaredConstructor(@NotNull Class<?> clazz, Class<?>... argClasses) throws NoSuchMethodException, IllegalAccessException {
        ArgSig sig = new ArgSig(argClasses);
        MethodHandle cached = getConstructorFromCache(declaredConstructorCache, clazz, sig);
        if (cached != null) return cached;
        Constructor<?> constructor = clazz.getDeclaredConstructor(argClasses);
        constructor.setAccessible(true);
        MethodHandle handle = LOOKUP.unreflectConstructor(constructor);
        putConstructorCache(declaredConstructorCache, clazz, sig, handle);
        return handle;
    }

    // ===== 创建实例（显式传入参数类型） =====

    @SuppressWarnings("unchecked")
    public static <T> T newInstance(Class<T> clazz, Class<?>[] argTypes, Object... args) throws Throwable {
        // 处理 null，视为空数组
        if (argTypes == null) {
            argTypes = new Class[0];
        }
        if (args == null) {
            args = new Object[0];
        }
        // 长度校验
        if (argTypes.length != args.length) {
            throw new IllegalArgumentException(
                "Argument types length (" + argTypes.length + ") does not match argument values length (" + args.length + ")"
            );
        }
        MethodHandle handle = getConstructor(clazz, argTypes);
        return (T) handle.invokeWithArguments(args);
    }

    @SuppressWarnings("unchecked")
    public static <T> T newDeclaredInstance(Class<T> clazz, Class<?>[] argTypes, Object... args) throws Throwable {
        if (argTypes == null) {
            argTypes = new Class[0];
        }
        if (args == null) {
            args = new Object[0];
        }
        if (argTypes.length != args.length) {
            throw new IllegalArgumentException(
                "Argument types length (" + argTypes.length + ") does not match argument values length (" + args.length + ")"
            );
        }
        MethodHandle handle = getDeclaredConstructor(clazz, argTypes);
        return (T) handle.invokeWithArguments(args);
    }

    // ===== 单例 =====

    @SuppressWarnings("unchecked")
    public static <T> T getSingletonClassInstance(Class<T> clazz) throws NoClassDefFoundError, ClassNotFoundException {
        if (clazz.isInstance(CrypticLib.plugin())) {
            return (T) CrypticLib.plugin();
        }
        return (T) singletonObjectMap.computeIfAbsent(clazz, k -> createSingleton(clazz));
    }

    private static <T> T createSingleton(Class<T> clazz) {
        if (clazz.isEnum()) {
            return clazz.getEnumConstants()[0];
        }
        // 优先尝试静态 INSTANCE 字段
        try {
            Field instanceField = ReflectionHelper.getDeclaredField(clazz, "INSTANCE");
            if (Modifier.isStatic(instanceField.getModifiers()) && instanceField.getType().equals(clazz)) {
                return getDeclaredFieldObj(instanceField, null);
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        // 其次尝试无参构造器
        try {
            Constructor<T> constructor = clazz.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create singleton instance of " + clazz.getName(), e);
        }
    }

    // ===== 缓存操作（直接存 MethodHandle） =====

    private static MethodHandle getMethodFromCache(
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, MethodHandle>>> cache,
        Class<?> clazz, String methodName, ArgSig sig
    ) {
        ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, MethodHandle>> nameMap = cache.get(clazz);
        if (nameMap == null) return null;
        ConcurrentHashMap<ArgSig, MethodHandle> sigMap = nameMap.get(methodName);
        return sigMap != null ? sigMap.get(sig) : null;
    }

    private static void putMethodCache(
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, MethodHandle>>> cache,
        Class<?> clazz, String methodName, ArgSig sig, MethodHandle handle
    ) {
        cache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(methodName, k -> new ConcurrentHashMap<>())
            .putIfAbsent(sig, handle);
    }

    private static MethodHandle getConstructorFromCache(
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<ArgSig, MethodHandle>> cache,
        Class<?> clazz, ArgSig sig
    ) {
        ConcurrentHashMap<ArgSig, MethodHandle> sigMap = cache.get(clazz);
        return sigMap != null ? sigMap.get(sig) : null;
    }

    private static void putConstructorCache(
        ConcurrentHashMap<Class<?>, ConcurrentHashMap<ArgSig, MethodHandle>> cache,
        Class<?> clazz, ArgSig sig, MethodHandle handle
    ) {
        cache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .putIfAbsent(sig, handle);
    }

    // ===== 内部类 =====

    private static final class FieldEntry {
        final Field field;
        final MethodHandle getter;
        final MethodHandle setter;

        FieldEntry(Field field, MethodHandle getter, MethodHandle setter) {
            this.field = field;
            this.getter = getter;
            this.setter = setter;
        }
    }

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