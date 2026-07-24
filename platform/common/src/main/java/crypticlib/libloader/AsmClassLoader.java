package crypticlib.libloader;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AsmClassLoader extends URLClassLoader {

    private static AsmClassLoader instance;
    private final Map<String, Class<?>> loadedClasses = new ConcurrentHashMap<>();

    public AsmClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public static synchronized void init(URL[] urls, ClassLoader parent) {
        if (instance == null) {
            instance = new AsmClassLoader(urls, parent);
        }
    }

    public static AsmClassLoader getInstance() {
        return instance;
    }

    public Class<?> defineClassPublic(String name, byte[] classBytes) {
        Class<?> clazz = defineClass(name, classBytes, 0, classBytes.length);
        loadedClasses.put(name, clazz);
        return clazz;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> clazz = loadedClasses.get(name);
        if (clazz != null) {
            return clazz;
        }
        // 先尝试从自己加载的 JAR 中查找
        try {
            return findClass(name);
        } catch (ClassNotFoundException e) {
            // 找不到再委托给父类加载器
            return super.loadClass(name);
        }
    }

}