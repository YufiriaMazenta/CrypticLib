package crypticlib.util;

import crypticlib.CrypticLib;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 反射相关工具类
 */
public class ReflectionHelper {

    private final static Map<String, Map<String, Field>> fieldCaches = new ConcurrentHashMap<>();
    private final static Map<Class<?>, Object> singletonObjectMap = new ConcurrentHashMap<>();
    private static Object PLUGIN_INSTANCE = null;

    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        Field cacheField = getFieldCache(clazz, fieldName);
        if (cacheField != null)
            return cacheField;
        try {
            Field field = clazz.getField(fieldName);
            putFieldCache(clazz, fieldName, field);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getDeclaredField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        Field cacheField = getFieldCache(clazz, fieldName);
        if (cacheField != null)
            return cacheField;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            putFieldCache(clazz, fieldName, field);
            return field;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    private static Field getFieldCache(Class<?> clazz, String fieldName) {
        String className = clazz.getName();
        if (fieldCaches.containsKey(className)) {
            Map<String, Field> classFieldCache = fieldCaches.get(className);
            if (classFieldCache.containsKey(fieldName)) {
                return classFieldCache.get(fieldName);
            }
        }
        return null;
    }

    private static void putFieldCache(Class<?> clazz, String fieldName, Field field) {
        String className = clazz.getName();
        if (fieldCaches.containsKey(className)) {
            fieldCaches.get(className).put(fieldName, field);
            return;
        }
        Map<String, Field> classFieldCache = new ConcurrentHashMap<>();
        classFieldCache.put(fieldName, field);
        fieldCaches.put(className, classFieldCache);
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

    public static Method getMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... argClasses) {
        try {
            return clazz.getMethod(methodName, argClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getDeclaredMethod(@NotNull Class<?> clazz, @NotNull String methodName, Class<?>... argClasses) {
        try {
            return clazz.getDeclaredMethod(methodName, argClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(@NotNull Method method, @Nullable Object invokeObj, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(invokeObj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeDeclaredMethod(@NotNull Method method, @Nullable Object invokeObj, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(invokeObj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Constructor<T> getConstructor(@NotNull Class<T> clazz, Class<?>... argClasses) {
        try {
            return clazz.getConstructor(argClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> Constructor<T> getDeclaredConstructor(@NotNull Class<T> obj, Class<?>... argClasses) {
        try {
            return obj.getDeclaredConstructor(argClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeConstructor(@NotNull Constructor<T> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T invokeDeclaredConstructor(@NotNull Constructor<T> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T newInstance(Class<T> clazz, Object... args) {
        Class<?>[] argClasses = new Class[args.length];
        for (int i = 0; i < args.length; i++) {
            argClasses[i] = args[i].getClass();
        }
        Constructor<T> constructor = getConstructor(clazz, argClasses);
        return invokeConstructor(constructor, args);
    }

    public static <T> T newDeclaredInstance(Class<T> clazz, Object... args) {
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
        if (PLUGIN_INSTANCE.getClass().isAssignableFrom(clazz)) {
            return (T) PLUGIN_INSTANCE;
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
                } catch (RuntimeException e) {
                    //当没有INSTANCE名字的变量时，则新建一个对象
                    object = ReflectionHelper.newDeclaredInstance(clazz, objects);
                    if (CrypticLib.debug()) {
                        e.printStackTrace();
                    }
                }
            }
            singletonObjectMap.put(clazz, object);
            return object;
        }
    }

    @Deprecated
    public static void setPluginInstance(Object pluginInstance) {
        if (PLUGIN_INSTANCE != null) {
            throw new UnsupportedOperationException("Plugin instance already set");
        }
        PLUGIN_INSTANCE = pluginInstance;
    }

    public static Object getPluginInstance() {
        return PLUGIN_INSTANCE;
    }

}
