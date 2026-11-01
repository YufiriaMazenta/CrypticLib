package crypticlib.env;

import crypticlib.env.dependency.Dependency;
import crypticlib.env.dependency.DependencyDownloader;
import crypticlib.util.IOHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * 依赖解析的加固项回归测试: 坐标路径穿越、POM 的 XXE 加固、下载原子性
 */
class DependencyHardeningTest {

    @TempDir
    Path tempDir;

    @Test
    void coordinateWithPathSeparatorIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Dependency.builder("com.example", "../../payload", "1.0").build());
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Dependency.builder("com/../example", "demo", "1.0").build());
        Assertions.assertThrows(IllegalArgumentException.class,
            () -> Dependency.builder("com.example", "..", "1.0").build());
    }

    @Test
    void versionWithPathSeparatorIsRejectedWhenBuildingPath() {
        //version 允许 latest/版本范围写法, 因此校验推迟到真正拼路径时
        Dependency dependency = Dependency.builder("com.example", "demo", "../../etc").build();
        File baseDir = new File(tempDir.toFile(), "libs");
        Assertions.assertThrows(IllegalArgumentException.class, () -> dependency.findFile(baseDir, "jar"));
    }

    @Test
    void normalCoordinateStaysInsideBaseDir() {
        Dependency dependency = Dependency.builder("com.example", "demo", "1.0.2").build();
        File baseDir = new File(tempDir.toFile(), "libs");
        File file = dependency.findFile(baseDir, "jar");
        Assertions.assertEquals("demo-1.0.2.jar", file.getName());
        Assertions.assertTrue(file.getAbsolutePath().startsWith(baseDir.getAbsolutePath()));
        Assertions.assertEquals(new File(baseDir, "com/example/demo/1.0.2/demo-1.0.2.jar").getAbsolutePath(),
            file.getAbsolutePath());
    }

    @Test
    void pomWithDoctypeIsRejectedBeforeEntitiesResolve() {
        String pom = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
            + "<!DOCTYPE project [<!ENTITY local SYSTEM \"file:///etc/passwd\">]>"
            + "<project><groupId>com.example</groupId><artifactId>demo</artifactId>"
            + "<version>1.0</version><description>&local;</description></project>";
        DependencyDownloader downloader = new DependencyDownloader(tempDir.toFile(), null);
        IOException error = Assertions.assertThrows(IOException.class, () -> downloader.loadDependencyFromInputStream(
            new ByteArrayInputStream(pom.getBytes(StandardCharsets.UTF_8))));
        //解析阶段就被 DOCTYPE 拦下, 实体内容不会出现在任何异常信息里
        Assertions.assertFalse(describe(error).contains("root:"));
    }

    @Test
    void downloadWritesTargetAndLeavesNoPartialFile() throws Exception {
        Path source = tempDir.resolve("source.txt");
        Files.write(source, "payload".getBytes(StandardCharsets.UTF_8));
        File target = new File(tempDir.toFile(), "target.txt");

        IOHelper.downloadFile(source.toUri().toURL(), target);

        Assertions.assertEquals("payload", new String(Files.readAllBytes(target.toPath()), StandardCharsets.UTF_8));
        try (Stream<Path> files = Files.list(tempDir)) {
            Assertions.assertTrue(files.noneMatch(path -> path.getFileName().toString().endsWith(".part")),
                "下载完成后不应残留 .part 临时文件");
        }
    }

    private static String describe(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        Throwable current = throwable;
        while (current != null) {
            builder.append(current).append('\n');
            current = current.getCause();
        }
        return builder.toString();
    }
}
