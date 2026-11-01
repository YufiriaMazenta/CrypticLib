package crypticlib.env.dependency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * 依赖已下载判定与路径拼装的回归测试
 */
class DependencyFastPathTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsPomPackagingDeclaration() throws IOException {
        File pom = writePom("<project><artifactId>demo</artifactId><packaging>pom</packaging></project>");
        Assertions.assertTrue(DependencyDownloader.declaresPomPackaging(pom));
    }

    @Test
    void detectsPomPackagingWithSurroundingWhitespace() throws IOException {
        File pom = writePom("<project>\n  <packaging>\n    POM\n  </packaging>\n</project>");
        Assertions.assertTrue(DependencyDownloader.declaresPomPackaging(pom));
    }

    @Test
    void pomWithoutPackagingIsNotTreatedAsPomOnly() throws IOException {
        //默认打包是 jar: 缺失 jar 时应重新下载, 不能当成纯 POM 依赖永久跳过
        File pom = writePom("<project><artifactId>demo</artifactId></project>");
        Assertions.assertFalse(DependencyDownloader.declaresPomPackaging(pom));
    }

    @Test
    void missingPomFileIsNotPomOnly() {
        Assertions.assertFalse(DependencyDownloader.declaresPomPackaging(new File(tempDir.toFile(), "absent.pom")));
    }

    @Test
    void findFileToleratesMissingBaseDir() {
        //依赖下载器的 libs 目录是 @Nullable, 此时只拼相对路径, 不做包含校验
        Dependency dependency = Dependency.builder("com.example", "demo", "1.0").build();
        File file = dependency.findFile(null, "jar");
        Assertions.assertEquals("demo-1.0.jar", file.getName());
    }

    private File writePom(String content) throws IOException {
        Path pom = tempDir.resolve("pom-" + Math.abs(content.hashCode()) + ".xml");
        Files.write(pom, content.getBytes(StandardCharsets.UTF_8));
        return pom.toFile();
    }
}
