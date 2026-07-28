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
    static Throwable initError;

    static {
        try {
            Field field = Unsafe.class.getDeclaredField("theUnsafe");
            field.setAccessible(true);
            unsafe = (Unsafe) field.get(null);
            Field lookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            Object lookupBase = unsafe.staticFieldBase(lookupField);
            long lookupOffset = unsafe.staticFieldOffset(lookupField);
            lookup = (MethodHandles.Lookup) unsafe.getObject(lookupBase, lookupOffset);
        } catch (Throwable t) {
            initError = t;
        }
    }

    /**
     * 将 JAR 文件注入到 ClassLoader
     */
    public static ClassLoader addPath(Path path) throws Throwable {
        if (unsafe == null || lookup == null) {
            throw new IllegalStateException(
                "ClassAppender 初始化失败: 无法访问 sun.misc.Unsafe 或 MethodHandles.Lookup.IMPL_LOOKUP。" +
                "在 JDK 24+ 上如以 --sun-misc-unsafe-memory-access=deny 运行, 请改为 --sun-misc-unsafe-memory-access=allow。",
                initError);
        }
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
        // Paper PaperPluginClassLoader — 注入到其 libraryLoader
        // PaperSimplePluginClassLoader.findClass() 不会 fallback 到 super.findClass()，
        // 因此往 ucp 注入的 URL 永远不会被遍历；但 PaperPluginClassLoader.loadClass() 的
        // 第二步会委托给 libraryLoader.loadClass()，而 libraryLoader 是标准 URLClassLoader，
        // 其 findClass 会查 ucp，所以注入到这里可以生效。
        else if (loaderClassName.equals("io.papermc.paper.plugin.entrypoint.classloader.PaperPluginClassLoader")) {
            addPathToPaperLibraryLoader(loader, file);
        }
        // Bukkit PluginClassLoader
        else {
            addURL(loader, ucp(loader), file);
        }

        return loader;
    }

    /**
     * 将 JAR 注入到 Paper PluginClassLoader 的 libraryLoader 中。
     * PaperPluginClassLoader.loadClass() 的查找顺序：
     *   1. super.loadClass → PaperSimplePluginClassLoader.findClass → 只查插件自身 JAR
     *   2. libraryLoader.loadClass → 标准 URLClassLoader.findClass → 查 ucp（我们注入到这里）
     *   3. PluginClassLoaderGroup → 查依赖插件
     */
    private static void addPathToPaperLibraryLoader(ClassLoader paperLoader, File file) throws Throwable {
        Field field = paperLoader.getClass().getDeclaredField("libraryLoader");
        long offset = unsafe.objectFieldOffset(field);
        URLClassLoader libraryLoader = (URLClassLoader) unsafe.getObject(paperLoader, offset);

        if (libraryLoader != null) {
            addURL(libraryLoader, ucp(URLClassLoader.class), file);
        } else {
            // libraryLoader 为 null 的极端情况，降级到 AppClassLoader
            ClassLoader app = ClassLoader.getSystemClassLoader();
            addURL(app, ucp(app.getClass()), file);
        }
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
        } catch (NoSuchMethodException e) {
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
