package crypticlib.libloader;

import crypticlib.CrypticLib;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
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

    private static Class<?> relocatedJarRelocatorClass;

    public static synchronized ClassLoader getAsmClassLoader() {
        if (AsmClassLoader.getInstance() == null) {
            initAsm();
        }
        return AsmClassLoader.getInstance();
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
        if (AsmClassLoader.getInstance() != null) {
            return;
        }
        try {
            String groupIdPath = ASM_GROUP.replace('.', '/');
            File asmDir = new File("plugins/" + CrypticLib.pluginName() + "/libs/" + groupIdPath);
            if (!asmDir.exists()) {
                asmDir.mkdirs();
            }

            File asmCoreJar = downloadArtifact(asmDir, ASM_ARTIFACT_CORE);
            File asmCommonsJar = downloadArtifact(asmDir, ASM_ARTIFACT_COMMONS);

            File relocatedCore = relocateJarBytes(asmDir, asmCoreJar, ASM_ARTIFACT_CORE);
            File relocatedCommons = relocateJarBytes(asmDir, asmCommonsJar, ASM_ARTIFACT_COMMONS);

            AsmClassLoader.init(
                new URL[]{relocatedCore.toURI().toURL(), relocatedCommons.toURI().toURL()},
                BootLoader.class.getClassLoader()
            );

            loadJarRelocatorClass(AsmClassLoader.getInstance());
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize ASM", e);
        }
    }

    private static void loadJarRelocatorClass(AsmClassLoader asmLoader) throws Exception {
        String classResourcePath = "/" + JarRelocator.class.getName().replace('.', '/') + ".class";
        try (InputStream is = BootLoader.class.getResourceAsStream(classResourcePath)) {
            if (is == null) {
                throw new RuntimeException("Cannot find JarRelocator class resource");
            }
            byte[] classBytes = readAllBytes(is);
            byte[] relocatedBytes = relocateClassBytes(classBytes);
            relocatedJarRelocatorClass = asmLoader.defineClassPublic(JarRelocator.class.getName(), relocatedBytes);
        }

        String innerClassResourcePath = "/" + JarRelocator.class.getName().replace('.', '/') + "$RelocateRemapper.class";
        try (InputStream is = BootLoader.class.getResourceAsStream(innerClassResourcePath)) {
            if (is == null) {
                throw new RuntimeException("Cannot find JarRelocator$RelocateRemapper class resource");
            }
            byte[] classBytes = readAllBytes(is);
            byte[] relocatedBytes = relocateClassBytes(classBytes);
            asmLoader.defineClassPublic(JarRelocator.class.getName() + "$RelocateRemapper", relocatedBytes);
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
        if (result.length < 10 || result[0] != (byte) 0xCA || result[1] != (byte) 0xFE) {
            return result;
        }

        int pos = 8;
        int constantPoolCount = ((result[pos] & 0xFF) << 8) | (result[pos + 1] & 0xFF);
        pos += 2;

        for (int i = 1; i < constantPoolCount; i++) {
            int tag = result[pos] & 0xFF;
            switch (tag) {
                case 1:
                    int length = ((result[pos + 1] & 0xFF) << 8) | (result[pos + 2] & 0xFF);
                    int strStart = pos + 3;
                    String utf8 = new String(result, strStart, length, StandardCharsets.UTF_8);
                    if (utf8.contains(OLD_PREFIX_STR)) {
                        String newUtf8 = utf8.replace(OLD_PREFIX_STR, NEW_PREFIX_STR).replace(OLD_PREFIX_STR.replace('/', '.'), NEW_PREFIX_STR.replace('/', '.'));
                        byte[] newBytes = newUtf8.getBytes(StandardCharsets.UTF_8);
                        if (newBytes.length == length) {
                            System.arraycopy(newBytes, 0, result, strStart, length);
                        }
                    }
                    pos += 3 + length;
                    break;
                case 7:
                case 8:
                case 16:
                case 19:
                case 20:
                    pos += 3;
                    break;
                case 9:
                case 10:
                case 11:
                case 3:
                case 4:
                case 12:
                case 17:
                case 18:
                    pos += 5;
                    break;
                case 5:
                case 6:
                    pos += 9;
                    i++;
                    break;
                case 15:
                    pos += 4;
                    break;
                default:
                    pos += 3;
                    break;
            }
        }
        return result;
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
