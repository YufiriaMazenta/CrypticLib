package crypticlib.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ReflectUtil {

    public static Method getMethod(Class<?> clazz, String methodName, Class<?>... argClasses) {
        try {
            return clazz.getMethod(methodName, argClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... argClasses) {
        try {
            return clazz.getDeclaredMethod(methodName, argClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeMethod(Method method, Object invokeObj, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(invokeObj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeDeclaredMethod(Method method, Object invokeObj, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(invokeObj, args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor<?> getConstructor(Class<?> clazz, Class<?>... argClasses) {
        try {
            return clazz.getConstructor(argClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Constructor<?> getDeclaredConstructor(Class<?> obj, Class<?>... argClasses) {
        try {
            return obj.getDeclaredConstructor(argClasses);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeConstructor(Constructor<?> constructor, Object... args) {
        try {
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object invokeDeclaredConstructor(Constructor<?> constructor, Object... args) {
        try {
            constructor.setAccessible(true);
            return constructor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
