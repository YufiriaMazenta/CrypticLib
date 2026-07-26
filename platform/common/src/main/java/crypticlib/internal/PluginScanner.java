package crypticlib.internal;

import crypticlib.util.IOHelper;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
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

    @ApiStatus.Internal
    public void scanJar(@NotNull File file) {
        try {
            scanJar(new JarFile(file));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void scanJar(@NotNull JarFile jarFile) {
        pluginClassMap.clear();
        annotatedClassesMap.clear();

        Enumeration<JarEntry> entries = jarFile.entries();
        ClassLoader classLoader = getClass().getClassLoader();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String entryName = entry.getName();
            //跳过 module-info 与 META-INF/(含多版本 jar 的 versions/N/) 等与运行时无关的条目，
            //它们在低版本 JVM 上 loadClass 会抛 UnsupportedClassVersionError 等 LinkageError
            if (entryName.startsWith("META-INF/")
                || entryName.equals("module-info.class")
                || entryName.endsWith("/module-info.class")) {
                continue;
            }
            if (!entryName.endsWith(".class")) {
                //TODO 考虑是否要存储文件
                continue;
            }
            //先做字节码级预扫描，只有引用了 crypticlib 的类才真正 loadClass，
            //避免把插件 shade 的大量依赖(kotlin-stdlib、gson 等)全部定义进 JVM
            if (!referencesCrypticLib(jarFile, entry)) {
                continue;
            }
            String className = entryName
                .replace('/', '.')
                .substring(0, entryName.length() - 6);
            try {
                Class<?> clazz = classLoader.loadClass(className);
                IOHelper.debug("Loaded class: " + className);
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
            } catch (LinkageError | ClassNotFoundException e) {
                //单个类无法加载(缺依赖、class 版本过高等)不应中断整个扫描，仅跳过该类
                IOHelper.debug("Failed to load class: " + className + ", " + e);
            } catch (Throwable throwable) {
                //其余非 IO 级异常同样只跳过该类，保证扫描继续
                IOHelper.debug("Failed to process class: " + className + ", " + throwable);
            }
        }

        try {
            jarFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 仅解析 class 文件的常量池，判断其是否引用了 crypticlib(类型、注解、方法签名等)，
     * 用于在 loadClass 之前过滤掉与 crypticlib 无关的 shaded 依赖类。
     * <p>
     * 该方法不定义任何类到 JVM，且不依赖运行时对目标 class 版本的支持。解析失败或遇到
     * 未知常量池标签时保守地返回 false(视为无关类，不加载)。
     *
     * @param jarFile 所在 jar
     * @param entry   class 条目
     * @return 常量池中是否出现 crypticlib 引用
     */
    private boolean referencesCrypticLib(@NotNull JarFile jarFile, @NotNull JarEntry entry) {
        try (DataInputStream in = new DataInputStream(new BufferedInputStream(jarFile.getInputStream(entry)))) {
            if (in.readInt() != 0xCAFEBABE) {
                return false;
            }
            in.readUnsignedShort(); //minor version
            in.readUnsignedShort(); //major version
            int constantPoolCount = in.readUnsignedShort();
            for (int i = 1; i < constantPoolCount; i++) {
                int tag = in.readUnsignedByte();
                switch (tag) {
                    case 1: //CONSTANT_Utf8
                        if (in.readUTF().contains("crypticlib")) {
                            return true;
                        }
                        break;
                    case 5: //CONSTANT_Long
                    case 6: //CONSTANT_Double
                        in.skipBytes(8);
                        i++; //Long/Double 占用两个常量池槽位
                        break;
                    case 7:  //CONSTANT_Class
                    case 8:  //CONSTANT_String
                    case 16: //CONSTANT_MethodType
                    case 19: //CONSTANT_Module
                    case 20: //CONSTANT_Package
                        in.skipBytes(2);
                        break;
                    case 15: //CONSTANT_MethodHandle
                        in.skipBytes(3);
                        break;
                    case 3:  //CONSTANT_Integer
                    case 4:  //CONSTANT_Float
                    case 9:  //CONSTANT_Fieldref
                    case 10: //CONSTANT_Methodref
                    case 11: //CONSTANT_InterfaceMethodref
                    case 12: //CONSTANT_NameAndType
                    case 17: //CONSTANT_Dynamic
                    case 18: //CONSTANT_InvokeDynamic
                        in.skipBytes(4);
                        break;
                    default:
                        //未知常量池标签，无法安全解析，保守跳过该类
                        return false;
                }
            }
        } catch (IOException | RuntimeException e) {
            return false;
        }
        return false;
    }

    public <T> List<Class<T>> getSubClasses(Class<T> clazz) {
        List<Class<T>> subClasses = new ArrayList<>();
        pluginClassMap.forEach((name, cachedClass) -> {
            if (clazz.isAssignableFrom(cachedClass)) {
                subClasses.add((Class<T>) cachedClass);
            }
        });
        return subClasses;
    }

    public @NotNull List<Class<?>> getAnnotatedClasses(@NotNull Class<? extends Annotation> annotationClass) {
        return annotatedClassesMap.containsKey(annotationClass) ? annotatedClassesMap.get(annotationClass) : new ArrayList<>();
    }

}