package crypticlib.libloader;

import crypticlib.CrypticLib;
import crypticlib.chat.MsgSender;
import crypticlib.command.CommandManager;
import crypticlib.internal.CrypticLibPlugin;
import crypticlib.scheduler.Scheduler;
import crypticlib.util.IOHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LibLoaderDownloadTest {

    private static final File FIXED_DIR = new File("build/test-relocate-output");

    @BeforeAll
    static void setup() throws Exception {
        FIXED_DIR.mkdirs();

        CrypticLibPlugin mockPlugin = new CrypticLibPlugin() {
            @Override
            public String pluginName() { return "TestPlugin"; }
            @Override
            public CommandManager<?, ?> commandManager() { return null; }
            @Override
            public Scheduler scheduler() { return null; }
            @Override
            public MsgSender msgSender() { return null; }
        };

        Field field = CrypticLib.class.getDeclaredField("crypticLibPlugin");
        field.setAccessible(true);
        field.set(null, mockPlugin);
    }

    @Test
    public void testDownloadJar() throws Exception {
        File jarFile = new File(FIXED_DIR, "gson-2.10.1.jar");
        IOHelper.downloadFile("https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar", jarFile);
        assertTrue(jarFile.exists());
        assertTrue(jarFile.length() > 1000);
        System.out.println("Downloaded: " + jarFile.getAbsolutePath() + " (" + jarFile.length() + " bytes)");
    }

    @Test
    public void testDownloadAndLoad() throws Exception {
        File jarFile = new File(FIXED_DIR, "gson-2.10.1.jar");
        if (!jarFile.exists()) {
            IOHelper.downloadFile("https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar", jarFile);
        }
        try (URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, null)) {
            Class<?> c = cl.loadClass("com.google.gson.Gson");
            assertNotNull(c);
            Object instance = c.getDeclaredConstructor().newInstance();
            assertNotNull(instance);
            System.out.println("Loaded: " + c.getName());
        }
    }

    @Test
    public void testDownloadAndRelocate() throws Exception {
        File jarFile = new File(FIXED_DIR, "gson-2.10.1.jar");
        if (!jarFile.exists()) {
            IOHelper.downloadFile("https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar", jarFile);
        }

        // 原始 JAR
        try (URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toURI().toURL()}, null)) {
            Class<?> c = cl.loadClass("com.google.gson.Gson");
            assertNotNull(c);
            System.out.println("Original: " + c.getName());
        }

        // Relocate
        File relocatedFile = new File(FIXED_DIR, "gson-relocated.jar");
        Map<String, String> relocateMap = Map.of("com.google.gson", "com.test.shaded.gson");
        BootLoader.relocate(jarFile, relocatedFile, relocateMap);
        assertTrue(relocatedFile.exists());
        System.out.println("Relocated: " + relocatedFile.getAbsolutePath() + " (" + relocatedFile.length() + " bytes)");

        // 验证 relocated JAR
        try (URLClassLoader cl = new URLClassLoader(new URL[]{relocatedFile.toURI().toURL()}, null)) {
            assertThrows(ClassNotFoundException.class, () -> cl.loadClass("com.google.gson.Gson"));

            Class<?> c = cl.loadClass("com.test.shaded.gson.Gson");
            assertNotNull(c);
            assertEquals("com.test.shaded.gson.Gson", c.getName());

            // CodeSource 指向 relocated JAR
            String codeSource = c.getProtectionDomain().getCodeSource().getLocation().getPath();
            assertTrue(codeSource.contains("relocated"), "CodeSource: " + codeSource);

            // JAR 内容检查
            java.util.jar.JarFile jar = new java.util.jar.JarFile(relocatedFile);
            boolean hasOriginal = false;
            boolean hasRelocated = false;
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (entry.getName().startsWith("com/google/gson")) hasOriginal = true;
                    if (entry.getName().startsWith("com/test/shaded/gson")) hasRelocated = true;
                }
            }
            jar.close();
            assertFalse(hasOriginal, "Should not contain com/google/gson");
            assertTrue(hasRelocated, "Should contain com/test/shaded/gson");

            Class<?> builder = cl.loadClass("com.test.shaded.gson.GsonBuilder");
            assertNotNull(builder);
            System.out.println("Relocated class: " + c.getName());
            System.out.println("Relocated builder: " + builder.getName());
        }
    }

    @Test
    public void testLoadKotlinWithRelocate() throws Exception {
        File kotlinJar = new File(FIXED_DIR, "kotlin-stdlib-1.9.24.jar");
        if (!kotlinJar.exists()) {
            IOHelper.downloadFile("https://repo.maven.apache.org/maven2/org/jetbrains/kotlin/kotlin-stdlib/1.9.24/kotlin-stdlib-1.9.24.jar", kotlinJar);
        }

        // 原始 JAR
        try (URLClassLoader cl = new URLClassLoader(new URL[]{kotlinJar.toURI().toURL()}, null)) {
            Class<?> c = cl.loadClass("kotlin.Unit");
            assertNotNull(c);
            assertEquals("kotlin.Unit", c.getName());
            System.out.println("Original Kotlin: " + c.getName());
        }

        // Relocate
        File relocated = new File(FIXED_DIR, "kotlin-relocated.jar");
        BootLoader.relocate(kotlinJar, relocated, Map.of("kotlin", "com.test.shaded.kotlin"));
        assertTrue(relocated.exists());
        System.out.println("Relocated Kotlin: " + relocated.getAbsolutePath());

        // 验证
        try (URLClassLoader cl = new URLClassLoader(new URL[]{relocated.toURI().toURL()}, null)) {
            assertThrows(ClassNotFoundException.class, () -> cl.loadClass("kotlin.Unit"));

            Class<?> c = cl.loadClass("com.test.shaded.kotlin.Unit");
            assertNotNull(c);
            assertEquals("com.test.shaded.kotlin.Unit", c.getName());

            Field instanceField = c.getField("INSTANCE");
            Object instance = instanceField.get(null);
            assertNotNull(instance);
            assertTrue(instance.getClass().getName().startsWith("com.test.shaded.kotlin"));

            String codeSource = c.getProtectionDomain().getCodeSource().getLocation().getPath();
            assertTrue(codeSource.contains("relocated"));

            java.util.jar.JarFile jar = new java.util.jar.JarFile(relocated);
            boolean hasOriginal = false;
            boolean hasRelocated = false;
            java.util.Enumeration<java.util.jar.JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                java.util.jar.JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    if (entry.getName().startsWith("kotlin/")) hasOriginal = true;
                    if (entry.getName().startsWith("com/test/shaded/kotlin/")) hasRelocated = true;
                }
            }
            jar.close();
            assertFalse(hasOriginal, "Should not contain kotlin/");
            assertTrue(hasRelocated, "Should contain com/test/shaded/kotlin/");
            System.out.println("Relocated Kotlin class: " + c.getName());
        }
    }

    @Test
    public void testCrossDependencyRelocate() throws Exception {
        // 下载 Gson（库 A）
        File gsonJar = new File(FIXED_DIR, "gson-2.10.1.jar");
        if (!gsonJar.exists()) {
            IOHelper.downloadFile("https://repo.maven.apache.org/maven2/com/google/code/gson/gson/2.10.1/gson-2.10.1.jar", gsonJar);
        }

        // 创建依赖 Gson 的小 JAR（库 B）
        File srcDir = new File(FIXED_DIR, "src/com/example/dependent");
        srcDir.mkdirs();
        File srcFile = new File(srcDir, "GsonWrapper.java");
        java.nio.file.Files.write(srcFile.toPath(), (
            "package com.example.dependent;\n" +
            "import com.google.gson.Gson;\n" +
            "import com.google.gson.JsonObject;\n" +
            "public class GsonWrapper {\n" +
            "    private final Gson gson = new Gson();\n" +
            "    public String toJson(Object obj) { return gson.toJson(obj); }\n" +
            "    public JsonObject parse(String json) { return gson.fromJson(json, JsonObject.class); }\n" +
            "}\n"
        ).getBytes());

        // 编译
        File classesDir = new File(FIXED_DIR, "classes");
        classesDir.mkdirs();
        ProcessBuilder pb = new ProcessBuilder(
            "javac",
            "-cp", gsonJar.getAbsolutePath(),
            "-d", classesDir.getAbsolutePath(),
            srcFile.getAbsolutePath()
        );
        pb.inheritIO();
        assertEquals(0, pb.start().waitFor(), "Compilation should succeed");

        // 打包 JAR
        File dependentJar = new File(FIXED_DIR, "dependent.jar");
        ProcessBuilder jarPb = new ProcessBuilder(
            "jar",
            "cf", dependentJar.getAbsolutePath(),
            "-C", classesDir.getAbsolutePath(), "."
        );
        jarPb.inheritIO();
        assertEquals(0, jarPb.start().waitFor());
        assertTrue(dependentJar.exists());
        System.out.println("Created dependent JAR: " + dependentJar.getAbsolutePath());

        // 原始：B + A 都能用
        try (URLClassLoader cl = new URLClassLoader(
            new URL[]{dependentJar.toURI().toURL(), gsonJar.toURI().toURL()}, null)) {
            Class<?> wrapperClass = cl.loadClass("com.example.dependent.GsonWrapper");
            Object wrapper = wrapperClass.getDeclaredConstructor().newInstance();
            String result = (String) wrapperClass.getMethod("toJson", Object.class).invoke(wrapper, "hello");
            System.out.println("Original B+A: toJson(\"hello\") = " + result);
        }

        // Relocate A
        File relocatedA = new File(FIXED_DIR, "gson-cross-relocated.jar");
        BootLoader.relocate(gsonJar, relocatedA, Map.of("com.google.gson", "com.shaded.gson"));
        System.out.println("Relocated A: " + relocatedA.getAbsolutePath());

        // 错误方式：只 relocate B，不包含 A 的映射
        File relocatedBWrong = new File(FIXED_DIR, "dependent-relocated-wrong.jar");
        BootLoader.relocate(dependentJar, relocatedBWrong, Map.of("com.example.dependent", "com.shaded.dependent"));
        System.out.println("Relocated B (wrong): " + relocatedBWrong.getAbsolutePath());

        try (URLClassLoader cl = new URLClassLoader(
            new URL[]{relocatedBWrong.toURI().toURL(), relocatedA.toURI().toURL()}, null)) {
            Class<?> wrapperClass = cl.loadClass("com.shaded.dependent.GsonWrapper");
            assertNotNull(wrapperClass);
            try {
                wrapperClass.getDeclaredConstructor().newInstance();
                System.out.println("Warning: should have failed");
            } catch (Exception e) {
                System.out.println("Expected failure (wrong): " + e.getCause().getClass().getName());
                assertTrue(e.getCause() instanceof NoClassDefFoundError || e.getCause() instanceof ClassNotFoundException);
            }
        }

        // 正确方式：relocate B 时同时包含 A 的映射
        File relocatedBCorrect = new File(FIXED_DIR, "dependent-relocated-correct.jar");
        BootLoader.relocate(dependentJar, relocatedBCorrect, Map.of(
            "com.example.dependent", "com.shaded.dependent",
            "com.google.gson", "com.shaded.gson"
        ));
        System.out.println("Relocated B (correct): " + relocatedBCorrect.getAbsolutePath());

        try (URLClassLoader cl = new URLClassLoader(
            new URL[]{relocatedBCorrect.toURI().toURL(), relocatedA.toURI().toURL()}, null)) {
            Class<?> wrapperClass = cl.loadClass("com.shaded.dependent.GsonWrapper");
            assertNotNull(wrapperClass);
            assertEquals("com.shaded.dependent.GsonWrapper", wrapperClass.getName());

            Object wrapper = wrapperClass.getDeclaredConstructor().newInstance();
            String result = (String) wrapperClass.getMethod("toJson", Object.class).invoke(wrapper, "hello");
            System.out.println("Correct B+A: toJson(\"hello\") = " + result);

            java.lang.reflect.Field gsonField = wrapperClass.getDeclaredField("gson");
            gsonField.setAccessible(true);
            Object gsonInstance = gsonField.get(wrapper);
            System.out.println("Gson class in B: " + gsonInstance.getClass().getName());
            assertTrue(gsonInstance.getClass().getName().startsWith("com.shaded.gson"));
        }

        System.out.println("Cross-dependency relocate test passed!");
    }

    @Test
    public void testLoadLibraryWithRelocate() throws Exception {
        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.google.code.gson:gson:2.10.1",
            Relocation.of("com.google.gson", "com.test.shaded.gson")
        );

        assertEquals("com.google.code.gson", library.groupId());
        assertEquals("gson", library.artifactId());
        assertEquals("2.10.1", library.version());
        assertFalse(library.relocations().isEmpty());

        File jarFile = new File(FIXED_DIR, "gson-2.10.1.jar");
        if (!jarFile.exists()) {
            String groupIdPath = library.groupId().replace('.', '/');
            String url = library.repository() + groupIdPath + "/" + library.artifactId() + "/" + library.version() + "/" + library.artifactId() + "-" + library.version() + ".jar";
            IOHelper.downloadFile(url, jarFile);
        }

        File relocatedFile = new File(FIXED_DIR, "gson-lib-relocated.jar");
        BootLoader.relocate(jarFile, relocatedFile, library.relocateMap());
        assertTrue(relocatedFile.exists());

        try (URLClassLoader cl = new URLClassLoader(new URL[]{relocatedFile.toURI().toURL()}, null)) {
            assertThrows(ClassNotFoundException.class, () -> cl.loadClass("com.google.gson.Gson"));
            Class<?> c = cl.loadClass("com.test.shaded.gson.Gson");
            assertNotNull(c);
            assertEquals("com.test.shaded.gson.Gson", c.getName());
            System.out.println("Library relocate: " + c.getName());
        }
    }
}
