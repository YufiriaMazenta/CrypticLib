package crypticlib.internal;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
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
    private final Map<Class<? extends Annotation>, List<Class<?>>> annotatedClassesMap = new ConcurrentHashMap<>();

    @Deprecated
    public void scanJar(@NotNull File file) {
        try {
            scanJar(new JarFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void scanJar(@NotNull JarFile jarFile) {
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
                    //添加注解缓存
                    for (Annotation annotation : clazz.getAnnotations()) {
                        Class<? extends Annotation> annotationClass = annotation.annotationType();
                        if (annotatedClassesMap.containsKey(annotationClass)) {
                            List<Class<?>> annotatedClasses = annotatedClassesMap.get(annotationClass);
                            if (!annotatedClasses.contains(clazz)) {
                                annotatedClasses.add(clazz);
                            }
                        } else {
                            List<Class<?>> annotatedClasses = new ArrayList<>();
                            annotatedClasses.add(clazz);
                            annotatedClassesMap.put(annotationClass, annotatedClasses);
                        }
                    }
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

    public <T> List<Class<T>> getSubClasses(Class<T> clazz) {
        List<Class<T>> subClasses = new ArrayList<>();
        pluginClassMap.forEach((name, cachedClass) -> {
            if (clazz.isAssignableFrom(cachedClass)) {
                subClasses.add(clazz);
            }
        });
        return subClasses;
    }

    public @NotNull List<Class<?>> getAnnotatedClasses(@NotNull Class<? extends Annotation> annotationClass) {
        return annotatedClassesMap.containsKey(annotationClass) ? annotatedClassesMap.get(annotationClass) : new ArrayList<>();
    }

}