package crypticlib.libloader;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LibLoaderTest {

    @Test
    public void testLibraryWithRelocate() {
        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.google.code.gson:gson:2.10.1",
            Relocation.of("com.google.code.gson", "com.test.shaded.gson")
        );

        assertEquals("com.google.code.gson", library.groupId());
        assertEquals("gson", library.artifactId());
        assertEquals("2.10.1", library.version());
        assertEquals("com.google.code.gson:gson:2.10.1", library.dependency());
        assertEquals("https://repo.maven.apache.org/maven2/", library.repository());
        assertFalse(library.relocations().isEmpty());
        assertEquals(1, library.relocations().size());
        assertEquals("com.google.code.gson", library.relocations().get(0).from());
        assertEquals("com.test.shaded.gson", library.relocations().get(0).to());
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
        assertTrue(library.relocations().isEmpty());
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
        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.example:b:1.0",
            Relocation.of("com.example.a", "com.shaded.a"),
            Relocation.of("com.example.b", "com.shaded.b")
        );

        assertEquals(2, library.relocations().size());
        assertEquals("com.example.a", library.relocations().get(0).from());
        assertEquals("com.shaded.a", library.relocations().get(0).to());
        assertEquals("com.example.b", library.relocations().get(1).from());
        assertEquals("com.shaded.b", library.relocations().get(1).to());
    }

    @Test
    public void testRelocateMapConversion() {
        Library library = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.example:b:1.0",
            Relocation.of("com.example.a", "com.shaded.a"),
            Relocation.of("com.example.b", "com.shaded.b")
        );

        java.util.Map<String, String> map = library.relocateMap();
        assertEquals(2, map.size());
        assertEquals("com.shaded.a", map.get("com.example.a"));
        assertEquals("com.shaded.b", map.get("com.example.b"));
    }

    @Test
    public void testRelocateMapEmpty() {
        Library library = new Library("com.example:lib:1.0");
        assertTrue(library.relocateMap().isEmpty());
    }

    @Test
    public void testGetClassLoaderForNonExistentLibrary() {
        ClassLoader classLoader = LibLoader.getClassLoader("non.existent:lib:1.0.0");
        assertNull(classLoader);
    }

    @Test
    public void testRelocationRecord() {
        Relocation r = Relocation.of("com.old", "com.new");
        assertEquals("com.old", r.from());
        assertEquals("com.new", r.to());
    }

    @Test
    public void testRelocationPercentSeparator() {
        // 支持用 % 代替 . 作为包名分隔符
        Relocation r = Relocation.of("com%example%a", "com%shaded%a");
        assertEquals("com.example.a", r.from());
        assertEquals("com.shaded.a", r.to());
    }

    @Test
    public void testRelocationMixedSeparator() {
        // 混合使用 . 和 %
        Relocation r = Relocation.of("com.example%a", "com.shaded%a");
        assertEquals("com.example.a", r.from());
        assertEquals("com.shaded.a", r.to());
    }

    @Test
    public void testLibraryHashInDependency() {
        // 支持在依赖字符串中用 # 避免 Shadow relocate 误伤
        Library lib = new Library("com.exa#mple:a#rtifact:1.0.0");
        assertEquals("com.example:artifact:1.0.0", lib.dependency());
        assertEquals("com.example", lib.groupId());
        assertEquals("artifact", lib.artifactId());
        assertEquals("1.0.0", lib.version());
    }

    @Test
    public void testLibraryHashWithRelocation() {
        // 组合使用 # 和 % 分隔符
        Library lib = new Library(
            "https://repo.maven.apache.org/maven2/",
            "com.goog#le.co#de.gso#n:gso#n:2.10.1",
            Relocation.of("com%google%gson", "com%shaded%gson")
        );
        assertEquals("com.google.code.gson:gson:2.10.1", lib.dependency());
        assertEquals("com.google.code.gson", lib.groupId());
        assertEquals("com.google.gson", lib.relocations().get(0).from());
        assertEquals("com.shaded.gson", lib.relocations().get(0).to());
    }

    @Test
    public void testLibraryDependencyFormat() {
        Library lib1 = new Library("group:artifact:version");
        assertEquals("group", lib1.groupId());
        assertEquals("artifact", lib1.artifactId());
        assertEquals("version", lib1.version());

        Library lib2 = new Library("org.example:my-lib:1.0.0-SNAPSHOT");
        assertEquals("org.example", lib2.groupId());
        assertEquals("my-lib", lib2.artifactId());
        assertEquals("1.0.0-SNAPSHOT", lib2.version());
    }
}
