package crypticlib.libloader;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class LibLoaderTest {

    @Test
    public void testLibraryWithRelocate() {
        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.google.code.gson:gson:2.10.1",
            Map.of("com.google.code.gson", "com.test.shaded.gson")
        );

        assertEquals("com.google.code.gson", library.groupId());
        assertEquals("gson", library.artifactId());
        assertEquals("2.10.1", library.version());
        assertEquals("com.google.code.gson:gson:2.10.1", library.dependency());
        assertEquals("https://repo.maven.apache.org/maven2/", library.repository());
        assertFalse(library.relocate().isEmpty());
        assertEquals("com.test.shaded.gson", library.relocate().get("com.google.code.gson"));
    }

    @Test
    public void testLibraryWithoutRelocate() {
        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "org.apache.commons:commons-lang3:3.12.0"
        );

        assertEquals("org.apache.commons", library.groupId());
        assertEquals("commons-lang3", library.artifactId());
        assertEquals("3.12.0", library.version());
        assertTrue(library.relocate().isEmpty());
    }

    @Test
    public void testLibraryDependencyParsing() {
        Library library = new Library("com.example:test-lib:1.0.0");

        assertEquals("com.example", library.groupId());
        assertEquals("test-lib", library.artifactId());
        assertEquals("1.0.0", library.version());
        assertEquals("com.example:test-lib:1.0.0", library.dependency());
        assertEquals(Library.REPOSITORY_MAVEN_CENTRAL, library.repository());
    }

    @Test
    public void testLibraryWithCustomRepository() {
        Library library = new Library(
            "https://jitpack.io",
            "com.github.User:Repo:1.0.0"
        );

        assertEquals("https://jitpack.io", library.repository());
        assertEquals("com.github.User", library.groupId());
        assertEquals("Repo", library.artifactId());
        assertEquals("1.0.0", library.version());
    }

    @Test
    public void testLibraryConstants() {
        assertNotNull(Library.REPOSITORY_MAVEN_CENTRAL);
        assertNotNull(Library.REPOSITORY_MAVEN_CENTRAL_MIRROR_ALI);
        assertNotNull(Library.REPOSITORY_JITPACK);
        assertNotNull(Library.REPOSITORY_SONATYPE);

        assertTrue(Library.REPOSITORY_MAVEN_CENTRAL.contains("maven.apache.org"));
        assertTrue(Library.REPOSITORY_JITPACK.contains("jitpack.io"));
    }

    @Test
    public void testMultipleRelocateRules() {
        Map<String, String> relocateMap = Map.of(
            "com.google.code.gson", "com.test.shaded.gson",
            "com.google.common", "com.test.shaded.guava"
        );

        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.google.code.gson:gson:2.10.1",
            relocateMap
        );

        assertEquals(2, library.relocate().size());
        assertEquals("com.test.shaded.gson", library.relocate().get("com.google.code.gson"));
        assertEquals("com.test.shaded.guava", library.relocate().get("com.google.common"));
    }

    @Test
    public void testGetClassLoaderForNonExistentLibrary() {
        // getClassLoader 在没有加载过库时应返回 null
        // 注意：不能调用 LibLoader.loadLibrary，因为需要 Bukkit 环境
        ClassLoader classLoader = LibLoader.getClassLoader("non.existent:lib:1.0.0");
        assertNull(classLoader);
    }

    @Test
    public void testLibraryDependencyFormat() {
        // 测试各种依赖格式
        Library lib1 = new Library("group:artifact:version");
        assertEquals("group", lib1.groupId());
        assertEquals("artifact", lib1.artifactId());
        assertEquals("version", lib1.version());

        Library lib2 = new Library("org.example:my-lib:1.0.0-SNAPSHOT");
        assertEquals("org.example", lib2.groupId());
        assertEquals("my-lib", lib2.artifactId());
        assertEquals("1.0.0-SNAPSHOT", lib2.version());
    }

    @Test
    public void testLibraryRelocateIsUnmodifiable() {
        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.test:lib:1.0",
            Map.of("com.test", "com.shaded.test")
        );

        // relocate 返回的 Map 应该是不可修改的
        assertThrows(UnsupportedOperationException.class, () -> {
            library.relocate().put("new.key", "new.value");
        });
    }
}
