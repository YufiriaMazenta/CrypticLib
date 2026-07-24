package crypticlib.dependency;

import crypticlib.CrypticLib;
import crypticlib.util.IOHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.URL;
import java.util.*;

/**
 * 依赖加载主入口
 */
public enum DependencyLoader {

    INSTANCE;

    public static final String DEFAULT_DEPENDENCY_FOLDER = "plugins/" + CrypticLib.pluginName() + "/libs";
    public static final String REPOSITORY_MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2";
    public static final String REPOSITORY_MAVEN_CENTRAL_MIRROR_ALI = "https://maven.aliyun.com/repository/central";
    public static final String REPOSITORY_JITPACK = "https://jitpack.io";
    public static final String REPOSITORY_SONATYPE = "https://oss.sonatype.org/content/groups/public";

    /**
     * 加载依赖
     *
     * @param dependency 依赖对象
     */
    public void loadDependency(@NotNull Dependency dependency) throws Throwable {
        // 检查 test 条件
        String test = dependency.getTest();
        if (test != null && !test.isEmpty()) {
            String className = test.startsWith("!") ? test.substring(1) : test;
            boolean negated = test.startsWith("!");
            boolean exists = ClassAppender.isExists(className);
            if (negated == exists) {
                return;
            }
        }

        String url = dependency.toString();
        String[] args = url.split(":");
        String repository = dependency.getRepository();
        File baseDir = new File(DEFAULT_DEPENDENCY_FOLDER);
        List<JarRelocation> relocation = dependency.getRelocations();
        boolean transitive = dependency.isTransitive();

        IOHelper.info("Loading " + args[0] + ":" + args[1] + ":" + args[2] + (transitive ? " (transitive)" : ""));

        DependencyDownloader downloader = new DependencyDownloader(baseDir, relocation);

        // 解析仓库
        if (repository == null || repository.isEmpty()) {
            repository = REPOSITORY_MAVEN_CENTRAL;
        }
        // 移除尾部的 /
        if (repository.endsWith("/")) {
            repository = repository.substring(0, repository.length() - 1);
        }
        downloader.addRepository(new Repository(repository));
        downloader.setDependencyScopes(new DependencyScope[]{dependency.getScope()});
        downloader.setTransitive(transitive);

        // 解析 POM 并收集所有依赖（主依赖 + 传递依赖）
        Set<Dependency> allDeps = new HashSet<>();

        File pomFile = new File(baseDir, String.format("%s/%s/%s/%s-%s.pom",
            args[0].replace('.', '/'), args[1], args[2], args[1], args[2]));
        File pomFile1 = new File(pomFile.getPath() + ".sha1");

        if (validation(pomFile, pomFile1)) {
            allDeps.addAll(downloader.loadDependencyFromInputStream(pomFile.toURI().toURL().openStream()));
        } else {
            String pom = String.format("%s/%s/%s/%s/%s-%s.pom",
                repository, args[0].replace('.', '/'), args[1], args[2], args[1], args[2]);
            IOHelper.info("Downloading " + args[0] + ":" + args[1] + ":" + args[2] + "...");
            allDeps.addAll(downloader.loadDependencyFromInputStream(new URL(pom).openStream()));
        }

        // 加载主依赖
        Dependency dep = new Dependency(args[0], args[1], args[2], DependencyScope.RUNTIME);
        dep.setExternal(dependency.isExternal());
        allDeps.addAll(downloader.loadDependency(downloader.getRepositories(), dep));

        // 注入所有依赖
        downloader.injectClasspath(allDeps);
        IOHelper.info("Done loading " + args[0] + ":" + args[1] + ":" + args[2]);
    }

    /**
     * 便捷方法：通过坐标加载依赖
     */
    public void loadDependency(@NotNull String coordinate) throws Throwable {
        loadDependency(Dependency.builder(coordinate).build());
    }

    private boolean validation(@NotNull File file, @NotNull File sha1File) {
        if (!file.exists() || !sha1File.exists()) {
            return false;
        }
        try {
            String expected = new String(java.nio.file.Files.readAllBytes(sha1File.toPath())).trim().split("\\s+")[0];
            String actual = sha1Hex(file);
            return expected.equalsIgnoreCase(actual);
        } catch (Exception e) {
            return false;
        }
    }

    @NotNull
    private String sha1Hex(@NotNull File file) throws Exception {
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
        try (java.io.InputStream is = java.nio.file.Files.newInputStream(file.toPath())) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) > 0) {
                digest.update(buf, 0, len);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
