package crypticlib.dependency;

import sun.misc.Unsafe;

import java.io.File;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

/**
 * ClassLoader 注入工具
 * 使用 Unsafe + MethodHandles 将 JAR 注入到运行时 ClassLoader
 */
public class ClassAppender {

    static MethodHandles.Lookup lookup;
    static Unsafe unsafe;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable ignore) {
        }
    }

    /**
     * 将 JAR 文件注入到 ClassLoader
     */
    public static ClassLoader addPath(Path path) throws Throwable {
        File file = new File(path.toUri().getPath());
        ClassLoader loader = DependencyLoader.class.getClassLoader();

        String loaderClassName = loader.getClass().getName();

        // Application ClassLoader (现代 JVM)
        if (loaderClassName.equals("jdk.internal.loader.ClassLoaders$AppClassLoader")) {
            addURL(loader, ucp(loader.getClass()), file);
        }
        // LaunchClassLoader (Hybrid/旧版 Forge)
        else if (loaderClassName.equals("net.minecraft.launchwrapper.LaunchClassLoader")) {
            MethodHandle methodHandle = lookup.findVirtual(URLClassLoader.class, "addURL", MethodType.methodType(void.class, java.net.URL.class));
            methodHandle.invoke(loader, file.toURI().toURL());
        }
        // Bukkit PluginClassLoader
        else {
            addURL(loader, ucp(loader), file);
        }

        return loader;
    }

    /**
     * 获取当前 ClassLoader
     */
    public static ClassLoader getClassLoader() {
        return DependencyLoader.class.getClassLoader();
    }

    /**
     * 检查类是否已存在
     */
    public static boolean isExists(String path) {
        try {
            Class.forName(path, false, getClassLoader());
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    private static void addURL(ClassLoader loader, Field ucpField, File file) throws Throwable {
        if (ucpField == null) {
            throw new IllegalStateException("ucp field not found");
        }
        Object ucp = unsafe.getObject(loader, unsafe.objectFieldOffset(ucpField));
        try {
            MethodHandle methodHandle = lookup.findVirtual(ucp.getClass(), "addURL", MethodType.methodType(void.class, URL.class));
            methodHandle.invoke(ucp, file.toURI().toURL());
        } catch (NoSuchMethodError e) {
            throw new IllegalStateException("Unsupported (classloader: " + loader.getClass().getName() + ", ucp: " + ucp.getClass().getName() + ")", e);
        }
    }

    private static Field ucp(ClassLoader loader) {
        try {
            return URLClassLoader.class.getDeclaredField("ucp");
        } catch (NoSuchFieldException ignored) {
            return ucp(loader.getClass());
        }
    }

    private static Field ucp(Class<?> clazz) {
        try {
            return clazz.getDeclaredField("ucp");
        } catch (NoSuchFieldException e) {
            Class<?> superclass = clazz.getSuperclass();
            if (superclass == Object.class) {
                return null;
            }
            return ucp(superclass);
        }
    }
}
