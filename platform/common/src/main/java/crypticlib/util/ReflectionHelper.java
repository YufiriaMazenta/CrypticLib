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

    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        Field cacheField = getFieldCache(fieldCaches, clazz, fieldName);
        if (cacheField != null)
            return cacheField;
        try {
            Field field = clazz.getField(fieldName);
            putFieldCache(fieldCaches, clazz, fieldName, field);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getDeclaredField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        Field cacheField = getFieldCache(declaredFieldCaches, clazz, fieldName);
        if (cacheField != null)
            return cacheField;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            putFieldCache(declaredFieldCaches, clazz, fieldName, field);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getFieldCache(Map<Class<?>, Map<String, Field>> caches, Class<?> clazz, String fieldName) {
        Map<String, Field> classFieldCache = caches.get(clazz);
        return classFieldCache != null ? classFieldCache.get(fieldName) : null;
    }

    private static void putFieldCache(Map<Class<?>, Map<String, Field>> caches, Class<?> clazz, String fieldName, Field field) {
        caches.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>()).put(fieldName, field);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getFieldObj(@NotNull Field field, @Nullable Object owner) {
        try {
            return (T) field.get(owner);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getDeclaredFieldObj(@NotNull Field field, @Nullable Object owner) {
        try {
            field.setAccessible(true);
            return (T) field.get(owner);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改一个变量的值
     * @param field 变量
     * @param owner 所属对象
     * @param value 新的值
     * @param <T> 变量的类型
     */
    public static <T> void setFieldObj(@NotNull Field field, @Nullable Object owner, @NotNull T value) {
        try {
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 修改一个私有变量的值
     * @param field 变量
     * @param owner 所属对象
     * @param value 新的值
     * @param <T> 变量的类型
     */
    public static <T> void setDeclaredFieldObj(@NotNull Field field, @Nullable Object owner, @NotNull T value) {
        try {
            field.setAccessible(true);
            field.set(owner, value);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... argClasses) throws NoSuchMethodException {
        // 查缓存
        ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>> nameMap = methodCache.get(clazz);
        if (nameMap != null) {
            ConcurrentHashMap<ArgSig, Optional<Method>> sigMap = nameMap.get(methodName);
            if (sigMap != null) {
                Optional<Method> opt = sigMap.get(new ArgSig(argClasses));
                if (opt != null) {
                    return opt.orElse(null);
                }
            }
        }
        // 缓存未命中
        Method found = clazz.getMethod(methodName, argClasses);
        cacheMethod(clazz, methodName, argClasses, found);
        return found;
    }

    public static Method getDeclaredMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... argClasses) throws NoSuchMethodException {
        // 查缓存
        ConcurrentHashMap<String, ConcurrentHashMap<ArgSig, Optional<Method>>> nameMap = declaredMethodCache.get(clazz);
        if (nameMap != null) {
            ConcurrentHashMap<ArgSig, Optional<Method>> sigMap = nameMap.get(methodName);
            if (sigMap != null) {
                Optional<Method> opt = sigMap.get(new ArgSig(argClasses));
                if (opt != null) {
                    return opt.orElse(null);
                }
            }
        }
        // 缓存未命中
        Method found = clazz.getDeclaredMethod(methodName, argClasses);
        declaredMethodCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(methodName, k -> new ConcurrentHashMap<>())
            .putIfAbsent(new ArgSig(argClasses), Optional.of(found));
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

    /**
     * 按参数个数查找所有同名候选方法（含缓存）。
     * 用于需要遍历重载的场景（如 setter 匹配）。
     *
     * @param clazz 目标类
     * @param methodName 方法名
     * @param paramCount 参数个数
     * @return 候选方法列表（可能为空）
     */
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

    /**
     * 缓存方法解析结果（包括负向结果 Optional.empty()）
     */
    private static void cacheMethod(@NotNull Class<?> clazz, @NotNull String methodName, @NotNull Class<?>[] argTypes, @Nullable Method method) {
        methodCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .computeIfAbsent(methodName, k -> new ConcurrentHashMap<>())
            .putIfAbsent(new ArgSig(argTypes), Optional.ofNullable(method));
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getConstructor(@NotNull Class<T> clazz, Class<?>... argClasses) throws NoSuchMethodException {
        // 查缓存
        ConcurrentHashMap<ArgSig, Optional<Constructor<?>>> sigMap = constructorCache.get(clazz);
        if (sigMap != null) {
            Optional<Constructor<?>> opt = sigMap.get(new ArgSig(argClasses));
            if (opt != null) {
                return (Constructor<T>) opt.orElse(null);
            }
        }
        // 缓存未命中
        Constructor<T> found = clazz.getConstructor(argClasses);
        constructorCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .putIfAbsent(new ArgSig(argClasses), Optional.of(found));
        return found;
    }

    @SuppressWarnings("unchecked")
    public static <T> Constructor<T> getDeclaredConstructor(@NotNull Class<T> clazz, Class<?>... argClasses) throws NoSuchMethodException {
        // 查缓存
        ConcurrentHashMap<ArgSig, Optional<Constructor<?>>> sigMap = declaredConstructorCache.get(clazz);
        if (sigMap != null) {
            Optional<Constructor<?>> opt = sigMap.get(new ArgSig(argClasses));
            if (opt != null) {
                return (Constructor<T>) opt.orElse(null);
            }
        }
        // 缓存未命中
        Constructor<T> found = clazz.getDeclaredConstructor(argClasses);
        declaredConstructorCache.computeIfAbsent(clazz, k -> new ConcurrentHashMap<>())
            .putIfAbsent(new ArgSig(argClasses), Optional.of(found));
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

    /**
     * 获取某类对应的实例，如果某类已经在注解处理器注册实例，则获取已经注册的实例
     * @param clazz 需要获取实例的类
     * @return 类对应的实例
     * @param <T> 类的类型
     */
    @SuppressWarnings("unchecked")
    public static <T> T getSingletonClassInstance(Class<T> clazz, Object...objects) throws NoClassDefFoundError, ClassNotFoundException {
        if (CrypticLib.plugin().getClass().isAssignableFrom(clazz)) {
            return (T) CrypticLib.plugin();
        } else if (singletonObjectMap.containsKey(clazz)) {
            return (T) singletonObjectMap.get(clazz);
        } else {
            T object;
            if (clazz.isEnum()) {
                //如果是枚举，则使用它的第一个枚举值
                object = clazz.getEnumConstants()[0];
            } else {
                try {
                    //尝试获取名为INSTANCE的静态变量，判断是否为该类的实例，若是则用作其实例
                    Field instanceField = ReflectionHelper.getDeclaredField(clazz, "INSTANCE");
                    if (Modifier.isStatic(instanceField.getModifiers()) && instanceField.getType().equals(clazz)) {
                        object = getDeclaredFieldObj(instanceField, null);
                    } else {
                        object = ReflectionHelper.newDeclaredInstance(clazz, objects);
                    }
                } catch (RuntimeException | NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    //当没有INSTANCE名字的变量时，则新建一个对象
                    try {
                        object = ReflectionHelper.newDeclaredInstance(clazz, objects);
                    } catch (NoSuchMethodException | InstantiationException |
                             IllegalAccessException | InvocationTargetException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
            singletonObjectMap.put(clazz, object);
            return object;
        }
    }

    /**
     * 参数类型签名，用作缓存 key
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

}
