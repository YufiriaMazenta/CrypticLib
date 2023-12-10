package crypticlib.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 反射相关工具类
 */
public class ReflectUtil {

    public static Field getField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        try {
            return clazz.getField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field getDeclaredField(@NotNull Class<?> clazz, @NotNull String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getFieldObj(@NotNull Field field, @Nullable Object owner) {
        try {
            return field.get(owner);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object getDeclaredFieldObj(@NotNull Field field, @Nullable Object owner) {
        try {
            field.setAccessible(true);
            return field.get(owner);
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

}
