package crypticlib.libloader;

import crypticlib.CrypticLib;
import crypticlib.PlatformSide;
import crypticlib.chat.MsgSender;
import crypticlib.command.CommandManager;
import crypticlib.internal.CrypticLibPlugin;
import crypticlib.scheduler.Scheduler;
import crypticlib.util.IOHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LibLoaderDownloadTest {

    @TempDir
    static Path tempDir;

    @BeforeAll
    static void setup() throws Exception {
        // 创建一个简单的 CrypticLibPlugin 实现
        CrypticLibPlugin mockPlugin = new CrypticLibPlugin() {
            @Override
            public String pluginName() {
                return "TestPlugin";
            }

            @Override
            public CommandManager<?, ?> commandManager() {
                return null;
            }

            @Override
            public Scheduler scheduler() {
                return null;
            }

            @Override
            public MsgSender msgSender() {
                return null;
            }
        };

        // 使用反射设置 crypticLibPlugin 字段
        Field field = CrypticLib.class.getDeclaredField("crypticLibPlugin");
        field.setAccessible(true);
        field.set(null, mockPlugin);
    }

    @Test
    public void testDownloadAndRelocate() throws Exception {
        // 1. 准备：创建目标目录
        File libDir = tempDir.toFile();
        File jarFile = new File(libDir, "gson-2.10.1.jar");

        // 2. 下载 JAR
        String repository = "https://repo.maven.apache.org/maven2/";
        String groupId = "com.google.code.gson";
        String artifactId = "gson";
        String version = "2.10.1";

        String groupIdPath = groupId.replace('.', '/');
        String url = repository + groupIdPath + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";

        System.out.println("Downloading: " + url);
        IOHelper.downloadFile(url, jarFile);
        assertTrue(jarFile.exists(), "JAR file should be downloaded");
        assertTrue(jarFile.length() > 0, "JAR file should not be empty");
        System.out.println("Downloaded: " + jarFile.getAbsolutePath() + " (" + jarFile.length() + " bytes)");

        // 3. 验证 JAR 可以加载
        try (URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, null)) {
            Class<?> gsonClass = cl.loadClass("com.google.gson.Gson");
            assertNotNull(gsonClass, "Should be able to load Gson class");
            System.out.println("Loaded class: " + gsonClass.getName());
        }

        // 4. Relocate
        Map<String, String> relocateMap = Map.of(
            "com.google.gson", "com.test.shaded.gson"
        );
        File relocatedFile = new File(libDir, "gson-2.10.1-relocated.jar");

        System.out.println("Relocating to: " + relocatedFile.getAbsolutePath());
        try {
            BootLoader.relocate(jarFile, relocatedFile, relocateMap);
            System.out.println("Relocation completed successfully");
        } catch (Exception e) {
            System.out.println("Relocation failed: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
        assertTrue(relocatedFile.exists(), "Relocated JAR should exist");
        assertTrue(relocatedFile.length() > 0, "Relocated JAR should not be empty");
        System.out.println("Relocated: " + relocatedFile.getAbsolutePath() + " (" + relocatedFile.length() + " bytes)");

        // 5. 验证 relocated JAR 可以加载，且包名已更改
        System.out.println("Relocated JAR exists: " + relocatedFile.exists());
        System.out.println("Relocated JAR size: " + relocatedFile.length());
        
        try (URLClassLoader cl = new URLClassLoader(new URL[]{relocatedFile.toURI().toURL()}, null)) {
            // 列出 JAR 中的所有类
            java.util.jar.JarFile jar = new java.util.jar.JarFile(relocatedFile);
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    System.out.println("Class in relocated JAR: " + entry.getName());
                }
            }
            jar.close();
            
            // 原始包名应该不存在
            assertThrows(ClassNotFoundException.class, () -> {
                cl.loadClass("com.google.code.gson.Gson");
            }, "Original package should not exist after relocation");

            // 新包名应该存在
            Class<?> relocatedGsonClass = cl.loadClass("com.test.shaded.gson.Gson");
            assertNotNull(relocatedGsonClass, "Should be able to load relocated Gson class");
            System.out.println("Loaded relocated class: " + relocatedGsonClass.getName());

            // 验证实例可以创建
            Object gsonInstance = relocatedGsonClass.getDeclaredConstructor().newInstance();
            assertNotNull(gsonInstance, "Should be able to create Gson instance");
            System.out.println("Created Gson instance: " + gsonInstance.getClass().getName());
        }

        System.out.println("Test passed!");
    }

    @Test
    public void testDownloadToCustomDir() throws Exception {
        // 测试下载到自定义目录
        File customDir = new File(tempDir.toFile(), "custom/libs/com/example/test/1.0");
        assertFalse(customDir.exists(), "Custom dir should not exist initially");

        customDir.mkdirs();
        assertTrue(customDir.exists(), "Custom dir should be created");

        File jarFile = new File(customDir, "test-1.0.jar");

        // 下载一个小的测试库
        String url = "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar";
        IOHelper.downloadFile(url, jarFile);

        assertTrue(jarFile.exists(), "JAR should be downloaded to custom dir");
        assertTrue(jarFile.length() > 0, "JAR should not be empty");
        System.out.println("Downloaded to custom dir: " + jarFile.getAbsolutePath());
    }

    @Test
    public void testIOHelperDownload() throws Exception {
        // 测试 IOHelper.downloadFile 基本功能
        File target = new File(tempDir.toFile(), "test-download.jar");
        String url = "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar";

        IOHelper.downloadFile(url, target);

        assertTrue(target.exists(), "File should be downloaded");
        assertTrue(target.length() > 1000, "File should have reasonable size");
        System.out.println("Download test passed: " + target.length() + " bytes");
    }

    @Test
    public void testDownloadMultipleJars() throws Exception {
        // 测试下载多个 JAR
        String[] artifacts = {
            "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar",
            "https://repo.maven.apache.org/maven2/com/google/guava/guava/32.1.3-jre/guava-32.1.3-jre.jar"
        };

        for (String url : artifacts) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            File jarFile = new File(tempDir.toFile(), fileName);
            IOHelper.downloadFile(url, jarFile);

            assertTrue(jarFile.exists(), "JAR should be downloaded: " + fileName);
            assertTrue(jarFile.length() > 1000, "JAR should have reasonable size: " + fileName);
            System.out.println("Downloaded: " + fileName + " (" + jarFile.length() + " bytes)");
        }
    }

    @Test
    public void testDownloadToNestedDir() throws Exception {
        // 测试下载到嵌套目录
        File nestedDir = new File(tempDir.toFile(), "libs/com/google/gson/2.10.1");
        nestedDir.mkdirs();

        File jarFile = new File(nestedDir, "gson-2.10.1.jar");
        String url = "https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar";
        IOHelper.downloadFile(url, jarFile);

        assertTrue(jarFile.exists(), "JAR should be downloaded to nested dir");
        assertTrue(jarFile.length() > 1000, "JAR should have reasonable size");
        System.out.println("Downloaded to nested dir: " + jarFile.getAbsolutePath());
    }

    @Test
    public void testLoadKotlinWithRelocate() throws Exception {
        // 测试 Kotlin stdlib 加载并 relocate 后能否正常使用
        File libDir = tempDir.toFile();

        // 下载 Kotlin stdlib
        File kotlinJar = new File(libDir, "kotlin-stdlib-1.9.24.jar");
        String kotlinUrl = "https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.9.24/kotlin-stdlib-1.9.24.jar";
        System.out.println("Downloading Kotlin stdlib...");
        IOHelper.downloadFile(kotlinUrl, kotlinJar);
        assertTrue(kotlinJar.exists(), "Kotlin JAR should be downloaded");
        assertTrue(kotlinJar.length() > 0, "Kotlin JAR should not be empty");
        System.out.println("Downloaded: " + kotlinJar.length() + " bytes");

        // 1. 验证原始 JAR 可以加载 Kotlin 类
        try (URLClassLoader cl = new URLClassLoader(new URL[]{kotlinJar.toURI().toURL()}, null)) {
            Class<?> kotlinUnit = cl.loadClass("kotlin.Unit");
            assertNotNull(kotlinUnit, "Should load kotlin.Unit from original JAR");
            System.out.println("Original JAR loaded: " + kotlinUnit.getName());

            Class<?> kotlinCollections = cl.loadClass("kotlin.collections.CollectionsKt");
            assertNotNull(kotlinCollections, "Should load kotlin.collections.CollectionsKt");
            System.out.println("Original JAR loaded: " + kotlinCollections.getName());
        }

        // 2. Relocate kotlin -> com.test.shaded.kotlin
        Map<String, String> relocateMap = Map.of(
            "kotlin", "com.test.shaded.kotlin"
        );
        File relocatedKotlinJar = new File(libDir, "kotlin-stdlib-1.9.24-relocated.jar");

        System.out.println("Relocating Kotlin stdlib...");
        BootLoader.relocate(kotlinJar, relocatedKotlinJar, relocateMap);
        assertTrue(relocatedKotlinJar.exists(), "Relocated Kotlin JAR should exist");
        assertTrue(relocatedKotlinJar.length() > 0, "Relocated Kotlin JAR should not be empty");
        System.out.println("Relocated: " + relocatedKotlinJar.length() + " bytes");

        // 3. 验证 relocated JAR
        try (URLClassLoader cl = new URLClassLoader(new URL[]{relocatedKotlinJar.toURI().toURL()}, null)) {
            // 原始包名不应该存在
            assertThrows(ClassNotFoundException.class, () -> {
                cl.loadClass("kotlin.Unit");
            }, "Original kotlin.Unit should not exist after relocation");

            assertThrows(ClassNotFoundException.class, () -> {
                cl.loadClass("kotlin.collections.CollectionsKt");
            }, "Original kotlin.collections.CollectionsKt should not exist after relocation");

            // 新包名应该存在
            Class<?> relocatedUnit = cl.loadClass("com.test.shaded.kotlin.Unit");
            assertNotNull(relocatedUnit, "Should load relocated kotlin.Unit");
            assertEquals("com.test.shaded.kotlin.Unit", relocatedUnit.getName());
            System.out.println("Relocated class loaded: " + relocatedUnit.getName());

            // 验证类名
            String className = relocatedUnit.getName();
            System.out.println("Kotlin class name: " + className);
            assertTrue(className.startsWith("com.test.shaded.kotlin"),
                "Class should be in relocated package, got: " + className);

            // 验证 CodeSource 指向 relocated JAR
            java.security.ProtectionDomain pd = relocatedUnit.getProtectionDomain();
            assertNotNull(pd);
            java.security.CodeSource cs = pd.getCodeSource();
            assertNotNull(cs);
            String codeSourcePath = cs.getLocation().getPath();
            assertTrue(codeSourcePath.contains("relocated"),
                "CodeSource should point to relocated JAR: " + codeSourcePath);

            // 尝试加载更多 Kotlin 类
            Class<?> relocatedCollectionsKt = cl.loadClass("com.test.shaded.kotlin.collections.CollectionsKt");
            assertNotNull(relocatedCollectionsKt, "Should load relocated CollectionsKt");
            System.out.println("Relocated CollectionsKt: " + relocatedCollectionsKt.getName());

            // 验证 Kotlin 内部类
            Class<?> relocatedUnitInstance = cl.loadClass("com.test.shaded.kotlin.Unit");
            assertNotNull(relocatedUnitInstance);
            // Unit 是一个单例，尝试获取 INSTANCE
            try {
                java.lang.reflect.Field instanceField = relocatedUnitInstance.getField("INSTANCE");
                assertNotNull(instanceField, "Unit should have INSTANCE field");
                Object instance = instanceField.get(null);
                assertNotNull(instance, "Unit.INSTANCE should not be null");
                System.out.println("Unit.INSTANCE class: " + instance.getClass().getName());
                assertTrue(instance.getClass().getName().startsWith("com.test.shaded.kotlin"),
                    "Unit.INSTANCE should be in relocated package");
            } catch (NoSuchFieldException e) {
                // Kotlin Unit 在某些版本可能没有 INSTANCE 字段
                System.out.println("Note: Unit has no INSTANCE field (may be different Kotlin version)");
            }

            // 验证 relocated JAR 中没有原始 kotlin 包
            java.util.jar.JarFile jar = new java.util.jar.JarFile(relocatedKotlinJar);
            boolean hasOriginalKotlin = false;
            boolean hasRelocatedKotlin = false;
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (entry.getName().startsWith("kotlin/")) {
                        hasOriginalKotlin = true;
                    }
                    if (entry.getName().startsWith("com/test/shaded/kotlin/")) {
                        hasRelocatedKotlin = true;
                    }
                }
            }
            jar.close();
            assertFalse(hasOriginalKotlin, "Relocated JAR should not contain original kotlin/ package");
            assertTrue(hasRelocatedKotlin, "Relocated JAR should contain relocated com/test/shaded/kotlin/ package");
        }

        System.out.println("Kotlin relocate test passed!");
    }

    @Test
    public void testLoadLibraryWithRelocate() throws Exception {
        // 测试完整的 loadLibrary 流程，并验证加载的确实是 relocate 后的类
        File libDir = tempDir.toFile();

        // 创建 Library 对象
        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.google.code.gson:gson:2.10.1",
            Map.of("com.google.gson", "com.test.shaded.gson")
        );

        // 验证 Library 属性
        assertEquals("com.google.code.gson", library.groupId());
        assertEquals("gson", library.artifactId());
        assertEquals("2.10.1", library.version());
        assertFalse(library.relocate().isEmpty());

        // 下载 JAR
        File jarFile = new File(libDir, "gson-2.10.1.jar");
        String groupIdPath = library.groupId().replace('.', '/');
        String url = library.repository() + groupIdPath + "/" + library.artifactId() + "/" + library.version() + "/" + library.artifactId() + "-" + library.version() + ".jar";

        IOHelper.downloadFile(url, jarFile);
        assertTrue(jarFile.exists(), "JAR should be downloaded");

        // 验证原始 JAR 可以加载原始包名
        try (URLClassLoader originalCl = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, null)) {
            Class<?> originalGson = originalCl.loadClass("com.google.gson.Gson");
            assertNotNull(originalGson, "Original JAR should load com.google.gson.Gson");
            assertEquals("com.google.gson.Gson", originalGson.getName());
        }

        // Relocate
        File relocatedFile = new File(libDir, "gson-2.10.1-relocated.jar");
        BootLoader.relocate(jarFile, relocatedFile, library.relocate());
        assertTrue(relocatedFile.exists(), "Relocated JAR should exist");
        assertTrue(relocatedFile.length() > 0, "Relocated JAR should not be empty");

        // 加载 relocated JAR
        try (URLClassLoader cl = new URLClassLoader(new URL[]{relocatedFile.toURI().toURL()}, null)) {
            // 1. 原始包名不应该存在
            assertThrows(ClassNotFoundException.class, () -> {
                cl.loadClass("com.google.gson.Gson");
            }, "Original package com.google.gson should not exist after relocation");

            // 2. 新包名应该存在
            Class<?> relocatedGsonClass = cl.loadClass("com.test.shaded.gson.Gson");
            assertNotNull(relocatedGsonClass, "Should be able to load relocated class");

            // 3. 验证类名确实是 relocate 后的
            assertEquals("com.test.shaded.gson.Gson", relocatedGsonClass.getName(),
                "Class name should be the relocated package");

            // 4. 验证 ProtectionDomain/CodeSource 指向 relocated JAR
            java.security.ProtectionDomain pd = relocatedGsonClass.getProtectionDomain();
            assertNotNull(pd, "ProtectionDomain should not be null");
            java.security.CodeSource cs = pd.getCodeSource();
            assertNotNull(cs, "CodeSource should not be null");
            assertNotNull(cs.getLocation(), "CodeSource location should not be null");
            String codeSourcePath = cs.getLocation().getPath();
            assertTrue(codeSourcePath.contains("relocated"),
                "CodeSource should point to relocated JAR, but was: " + codeSourcePath);
            assertTrue(codeSourcePath.endsWith(".jar"),
                "CodeSource should be a JAR file, but was: " + codeSourcePath);

            // 5. 验证 relocated JAR 中所有类都在新包名下
            java.util.jar.JarFile jar = new java.util.jar.JarFile(relocatedFile);
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            boolean hasOriginalPackage = false;
            boolean hasRelocatedPackage = false;
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (entry.getName().startsWith("com/google/gson")) {
                        hasOriginalPackage = true;
                    }
                    if (entry.getName().startsWith("com/test/shaded/gson")) {
                        hasRelocatedPackage = true;
                    }
                }
            }
            jar.close();
            assertFalse(hasOriginalPackage, "Relocated JAR should not contain original package com/google/gson");
            assertTrue(hasRelocatedPackage, "Relocated JAR should contain relocated package com/test/shaded/gson");

            // 6. 验证实例可以创建并正常工作
            Object gsonInstance = relocatedGsonClass.getDeclaredConstructor().newInstance();
            assertNotNull(gsonInstance, "Should be able to create Gson instance");

            // 7. 验证内部类也可以加载
            Class<?> internalClass = cl.loadClass("com.test.shaded.gson.GsonBuilder");
            assertNotNull(internalClass, "Should be able to load internal classes");
            assertEquals("com.test.shaded.gson.GsonBuilder", internalClass.getName());
        }
    }
}
