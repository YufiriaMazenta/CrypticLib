package crypticlib.annotation;

import crypticlib.util.ReflectUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * CrypticLib的注解处理器，暂时只支持处理类注解
 */
public enum AnnotationProcessor {

    INSTANCE;

    private final Map<Class<?>, Object> singletonObjectMap;
    private final Map<Class<? extends Annotation>, BiConsumer<Annotation, Class<?>>> classAnnotationProcessorMap;
    private final Map<Class<? extends Annotation>, ProcessPriority> annotationProcessorPriorityMap;

    AnnotationProcessor() {
        singletonObjectMap = new ConcurrentHashMap<>();
        classAnnotationProcessorMap = new ConcurrentHashMap<>();
        annotationProcessorPriorityMap = new ConcurrentHashMap<>();
    }

    @Deprecated
    public void scanJar(File file) {
        try {
            scanJar(new JarFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void scanJar(JarFile jarFile) {
        Enumeration<JarEntry> entries = jarFile.entries();
        ClassLoader classLoader = getClass().getClassLoader();
        Map<ProcessPriority, List<Runnable>> processTaskCache = new HashMap<>();
        while (entries.hasMoreElements()) {
            try {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class")) {
                    continue;
                }
                String className = entry.getName()
                    .replace('/', '.')
                    .substring(0, entry.getName().length() - 6);
                Class<?> clazz = classLoader.loadClass(className);

                //处理类级别的注解
                for (Class<? extends Annotation> annotationClass : classAnnotationProcessorMap.keySet()) {
                    if (!clazz.isAnnotationPresent(annotationClass))
                        continue;
                    ProcessPriority priority = annotationProcessorPriorityMap.get(annotationClass);
                    Annotation annotation = clazz.getAnnotation(annotationClass);
                    Runnable processTask = () -> classAnnotationProcessorMap.get(annotationClass).accept(annotation, clazz);
                    if (processTaskCache.containsKey(priority)) {
                        processTaskCache.get(priority).add(processTask);
                    } else {
                        List<Runnable> runnableList = new ArrayList<>();
                        runnableList.add(processTask);
                        processTaskCache.put(priority, runnableList);
                    }
                }
            } catch (ClassNotFoundException | NoClassDefFoundError ignored) {
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        }

        for (ProcessPriority priority : ProcessPriority.values()) {
            List<Runnable> tasks = processTaskCache.get(priority);
            if (tasks == null)
                continue;
            for (Runnable task : tasks) {
                task.run();
            }
        }

        try {
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 注册一个类注解处理器
     * @param annotationClass 注册的注解类
     * @param annotationProcessor 注解的处理器
     * @param priority 处理器的优先级，从LOWEST往HIGHEST依次执行
     */
    public AnnotationProcessor regClassAnnotationProcessor(
        @NotNull Class<? extends Annotation> annotationClass,
        @NotNull BiConsumer<Annotation, Class<?>> annotationProcessor,
        @NotNull ProcessPriority priority
    ) {
        classAnnotationProcessorMap.put(annotationClass, annotationProcessor);
        annotationProcessorPriorityMap.put(annotationClass, priority);
        return this;
    }

    /**
     * 注册一个类注解处理器，优先级为NORMAL
     * @param annotationClass 注册的注解类
     * @param annotationProcessor 注解的处理器
     */
    public AnnotationProcessor regClassAnnotationProcessor(
        @NotNull Class<? extends Annotation> annotationClass,
        @NotNull BiConsumer<Annotation, Class<?>> annotationProcessor
    ) {
        return regClassAnnotationProcessor(annotationClass, annotationProcessor, ProcessPriority.NORMAL);
    }

    /**
     * 获取某类对应的实例，如果某类已经在注解处理器注册实例，则获取已经注册的实例
     * @param clazz 需要获取实例的类
     * @return 类对应的实例
     * @param <T> 类的类型
     */
    public <T> T getClassInstance(Class<T> clazz, Object...objects) {
        if (singletonObjectMap.containsKey(clazz)) {
            return (T) singletonObjectMap.get(clazz);
        } else {
            T t;
            if (clazz.isEnum()) {
                t = clazz.getEnumConstants()[0];
            } else {
                t = ReflectUtil.newDeclaredInstance(clazz, objects);
            }
            singletonObjectMap.put(clazz, t);
            return t;
        }
    }

    public enum ProcessPriority {
        LOWEST, LOW, NORMAL, HIGH, HIGHEST
    }

}
