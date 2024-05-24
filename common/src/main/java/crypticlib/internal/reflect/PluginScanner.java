package crypticlib.internal.reflect;

import crypticlib.Plugin;
import crypticlib.internal.Platform;
import crypticlib.internal.annotation.PlatformSide;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * CrypticLib的插件扫描器
 */
public enum PluginScanner {

    INSTANCE;

    //插件的所有类
    private final Map<String, Class<?>> pluginClassMap = new ConcurrentHashMap<>();

    @Deprecated
    public void scanJar(File file) {
        try {
            scanJar(new JarFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void scanJar(JarFile jarFile) {
        pluginClassMap.clear();

        Enumeration<JarEntry> entries = jarFile.entries();
        ClassLoader classLoader = getClass().getClassLoader();
        while (entries.hasMoreElements()) {
            try {
                JarEntry entry = entries.nextElement();
                boolean isClassFile = entry.getName().endsWith(".class");
                if (isClassFile) {
                    String className = entry.getName()
                        .replace('/', '.')
                        .substring(0, entry.getName().length() - 6);
                    Class<?> clazz = classLoader.loadClass(className);
                    pluginClassMap.put(className, clazz);
                } else {
                    //TODO 考虑是否要存储文件
                }
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        try {
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取所有Plugin类的实现类
     * @return Plugin类的继承类
     */
    public @NotNull Class<? extends Plugin> findPluginImplClass() {
        return findPluginImplClass(null);
    }

    /**
     * 获取所有对应平台中Plugin类的实现类
     * @param platform 实现平台
     * @return 对应平台中Plugin类的实现类
     */
    public @NotNull Class<? extends Plugin> findPluginImplClass(@Nullable Platform platform) {
        if (pluginClassMap.isEmpty()) {
            throw new RuntimeException("The plugin has not scanned the class yet");
        }
        for (Class<?> clazz : pluginClassMap.values()) {
            if (Plugin.class.isAssignableFrom(clazz)) {
                if (Plugin.class.equals(clazz)) {
                    continue;
                }
                if (platform == null) {
                    return clazz.asSubclass(Plugin.class);
                } else {
                    if (!clazz.isAnnotationPresent(PlatformSide.class)) {
                        return clazz.asSubclass(Plugin.class);
                    } else {
                        PlatformSide platformSide = clazz.getAnnotation(PlatformSide.class);
                        if (Arrays.asList(platformSide.platform()).contains(platform)) {
                            return clazz.asSubclass(Plugin.class);
                        }
                    }
                }

            }
        }
        throw new RuntimeException("Unable to find plugin's implementation class");
    }

    public <T> List<Class<T>> getSubClasses(Class<T> clazz) {
        List<Class<T>> subClasses = new ArrayList<>();
        pluginClassMap.forEach((name, cachedClass) -> {
            if (clazz.isAssignableFrom(cachedClass)) {
                subClasses.add(clazz);
            }
        });
        return subClasses;
    }

}