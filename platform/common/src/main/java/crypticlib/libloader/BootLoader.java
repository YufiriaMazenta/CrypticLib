package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class BootLoader {

    private static final String ASM_GROUP = "org.ow2.asm";
    private static final String ASM_ARTIFACT_CORE = "asm";
    private static final String ASM_ARTIFACT_COMMONS = "asm-commons";
    private static final String ASM_VERSION = "9.10.1";
    private static final String MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/";

    private static final String OLD_PREFIX_STR = "org/objectweb/asm";
    private static final String NEW_PREFIX_STR = "crypticlib/lib/asm";
    private static final byte[] OLD_PREFIX = OLD_PREFIX_STR.getBytes(StandardCharsets.UTF_8);
    private static final byte[] NEW_PREFIX = NEW_PREFIX_STR.getBytes(StandardCharsets.UTF_8);

    private static URLClassLoader asmClassLoader;
    private static Class<?> relocatedJarRelocatorClass;

    public static synchronized ClassLoader getAsmClassLoader() {
        if (asmClassLoader == null) {
            initAsm();
        }
        return asmClassLoader;
    }

    public static void relocate(File input, File output, Map<String, String> relocation) {
        try {
            if (relocatedJarRelocatorClass == null) {
                getAsmClassLoader();
            }
            Object instance = relocatedJarRelocatorClass
                .getConstructor(File.class, File.class, Map.class)
                .newInstance(input, output, relocation);
            Method runMethod = relocatedJarRelocatorClass.getMethod("run");
            runMethod.invoke(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to relocate", e);
        }
    }

    private static synchronized void initAsm() {
        if (asmClassLoader != null) {
            return;
        }
        try {
            File libDir = new File("libs");
            if (!libDir.exists()) {
                libDir.mkdirs();
            }

            File asmCoreJar = downloadArtifact(libDir, ASM_ARTIFACT_CORE);
            File asmCommonsJar = downloadArtifact(libDir, ASM_ARTIFACT_COMMONS);

            File relocatedCore = relocateJarBytes(libDir, asmCoreJar, ASM_ARTIFACT_CORE);
            File relocatedCommons = relocateJarBytes(libDir, asmCommonsJar, ASM_ARTIFACT_COMMONS);

            URLClassLoader loader = new URLClassLoader(
                new URL[]{relocatedCore.toURI().toURL(), relocatedCommons.toURI().toURL()},
                BootLoader.class.getClassLoader()
            );

            loadJarRelocatorClass(loader);

            asmClassLoader = loader;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ASM", e);
        }
    }

    private static void loadJarRelocatorClass(ClassLoader asmLoader) throws Exception {
        String classResourcePath = "/" + JarRelocator.class.getName().replace('.', '/') + ".class";
        try (InputStream is = BootLoader.class.getResourceAsStream(classResourcePath)) {
            if (is == null) {
                throw new RuntimeException("Cannot find JarRelocator class resource");
            }
            byte[] classBytes = readAllBytes(is);
            byte[] relocatedBytes = relocateClassBytes(classBytes);

            Method defineClass = ClassLoader.class.getDeclaredMethod(
                "defineClass", String.class, byte[].class, int.class, int.class
            );
            defineClass.setAccessible(true);
            relocatedJarRelocatorClass = (Class<?>) defineClass.invoke(
                asmLoader,
                JarRelocator.class.getName(),
                relocatedBytes,
                0,
                relocatedBytes.length
            );
        }
    }

    private static File downloadArtifact(@NotNull File libDir, @NotNull String artifactId) throws IOException {
        String fileName = artifactId + "-" + ASM_VERSION + ".jar";
        File target = new File(libDir, fileName);
        if (target.exists()) {
            return target;
        }

        String groupIdPath = ASM_GROUP.replace('.', '/');
        String url = MAVEN_CENTRAL + groupIdPath + "/" + artifactId + "/" + ASM_VERSION + "/" + artifactId + "-" + ASM_VERSION + ".jar";

        try (InputStream is = new URL(url).openStream()) {
            Files.copy(is, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return target;
    }

    private static File relocateJarBytes(@NotNull File libDir, @NotNull File input, @NotNull String artifactId) throws IOException {
        String relocatedName = artifactId + "-" + ASM_VERSION + "-relocated.jar";
        File output = new File(libDir, relocatedName);
        if (output.exists()) {
            return output;
        }

        try (JarFile jarFile = new JarFile(input);
             JarOutputStream jos = new JarOutputStream(new FileOutputStream(output))) {
            for (JarEntry entry : jarFile.stream().toArray(JarEntry[]::new)) {
                String name = entry.getName();
                String mappedName = relocateEntryName(name);
                jos.putNextEntry(new JarEntry(mappedName));

                if (name.endsWith(".class")) {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        byte[] classBytes = readAllBytes(is);
                        byte[] relocatedBytes = relocateClassBytes(classBytes);
                        jos.write(relocatedBytes);
                    }
                } else {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        byte[] bytes = readAllBytes(is);
                        jos.write(bytes);
                    }
                }
                jos.closeEntry();
            }
        }
        return output;
    }

    private static byte[] relocateClassBytes(byte[] classBytes) {
        byte[] result = Arrays.copyOf(classBytes, classBytes.length);
        int oldLen = OLD_PREFIX.length;

        for (int i = 0; i <= result.length - oldLen; i++) {
            if (matchesAt(result, i, OLD_PREFIX)) {
                System.arraycopy(NEW_PREFIX, 0, result, i, NEW_PREFIX.length);
                i += oldLen - 1;
            }
        }
        return result;
    }

    private static boolean matchesAt(byte[] data, int offset, byte[] pattern) {
        if (offset + pattern.length > data.length) {
            return false;
        }
        for (int i = 0; i < pattern.length; i++) {
            if (data[offset + i] != pattern[i]) {
                return false;
            }
        }
        return true;
    }

    private static String relocateEntryName(String name) {
        String oldSlash = OLD_PREFIX_STR;
        String newSlash = NEW_PREFIX_STR;
        String oldDot = OLD_PREFIX_STR.replace('/', '.');
        String newDot = NEW_PREFIX_STR.replace('/', '.');

        if (name.startsWith(oldSlash)) {
            return newSlash + name.substring(oldSlash.length());
        }
        return name.replace(oldDot, newDot);
    }

    private static byte[] readAllBytes(@NotNull InputStream is) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((bytesRead = is.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        return bos.toByteArray();
    }

}
