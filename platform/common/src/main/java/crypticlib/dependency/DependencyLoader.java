package crypticlib.dependency;

import crypticlib.CrypticLib;
import crypticlib.util.IOHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * 依赖加载主入口
 */
public enum DependencyLoader {

    INSTANCE;

    public static final String DEFAULT_DEPENDENCY_FOLDER = "plugins/" + CrypticLib.plugin().pluginName() + "/libs";

    /**
     * 加载依赖
     *
     * @param dependency 依赖对象
     */
    public void loadDependency(@NotNull Dependency dependency) throws Throwable {
        // 检查 test 条件
        String test = dependency.test();
        if (test != null && !test.isEmpty()) {
            String className = test.startsWith("!") ? test.substring(1) : test;
            boolean negated = test.startsWith("!");
            boolean exists = ClassAppender.isExists(className);
            if (negated == exists) {
                return;
            }
        }

        List<Repository> repos = dependency.repositories();
        File baseDir = new File(DEFAULT_DEPENDENCY_FOLDER);
        List<JarRelocation> relocation = dependency.relocations();
        boolean transitive = dependency.isTransitive();

        // 解析仓库列表，从上到下尝试
        List<Repository> repositories = repos.isEmpty()
            ? Arrays.asList(Repository.MAVEN_CENTRAL_MIRROR_ALI, Repository.MAVEN_CENTRAL)
            : repos;

        // 版本未确定(latest / 版本范围 / 属性表达式)时, 先解析出真实版本号再拼 URL,
        // 否则会拼出字面 "latest" 的路径导致全仓库 404
        if (dependency.version() == null) {
            dependency.checkVersion(repositories, baseDir);
            if (dependency.version() == null) {
                throw new IOException("Unable to resolve version for "
                    + dependency.groupId() + ":" + dependency.artifactId());
            }
        }

        String url = dependency.toString();
        String[] args = url.split(":");

        CrypticLib.info("Loading " + args[0] + ":" + args[1] + ":" + args[2] + (transitive ? " (transitive)" : ""));

        DependencyDownloader downloader = new DependencyDownloader(baseDir, relocation);

        for (Repository repository : repositories) {
            downloader.addRepository(repository);
        }
        downloader.setTransitive(transitive);

        // 解析 POM 并收集所有依赖（主依赖 + 传递依赖）
        Set<Dependency> allDeps = new HashSet<>();

        File pomFile = new File(baseDir, String.format("%s/%s/%s/%s-%s.pom",
            args[0].replace('.', '/'), args[1], args[2], args[1], args[2]));
        File pomFile1 = new File(pomFile.getPath() + ".sha1");

        if (validation(pomFile, pomFile1)) {
            try (InputStream in = pomFile.toURI().toURL().openStream()) {
                allDeps.addAll(downloader.loadDependencyFromInputStream(in));
            }
        } else {
            // 从上到下尝试每个仓库下载 POM
            IOException lastError = null;
            for (Repository repo : downloader.repositories()) {
                String pom = String.format("%s/%s/%s/%s/%s-%s.pom",
                    repo.url(), args[0].replace('.', '/'), args[1], args[2], args[1], args[2]);
                try {
                    CrypticLib.info("Downloading " + args[0] + ":" + args[1] + ":" + args[2] + " from " + repo.url() + "...");
                    URLConnection conn = new URL(pom).openConnection();
                    conn.setConnectTimeout(15000);
                    conn.setReadTimeout(30000);
                    conn.setRequestProperty("User-Agent", "CrypticLib");
                    try (InputStream in = conn.getInputStream()) {
                        allDeps.addAll(downloader.loadDependencyFromInputStream(in));
                    }
                    lastError = null;
                    break;
                } catch (IOException e) {
                    lastError = e;
                }
            }
            if (lastError != null) {
                throw lastError;
            }
        }

        // 加载主依赖
        Dependency dep = new Dependency(args[0], args[1], args[2], DependencyScope.RUNTIME);
        dep.setExternal(dependency.isExternal());
        allDeps.addAll(downloader.loadDependency(downloader.repositories(), dep));

        // 注入所有依赖
        downloader.injectClasspath(allDeps);
        CrypticLib.info("Done loading " + args[0] + ":" + args[1] + ":" + args[2]);
    }

    /**
     * 便捷方法：通过坐标加载依赖
     */
    public void loadDependency(@NotNull String coordinate) throws Throwable {
        loadDependency(Dependency.builder(coordinate).build());
    }

    private boolean validation(@NotNull File file, @NotNull File sha1File) {
        return IOHelper.validateSha1(file, sha1File);
    }

    @NotNull
    private String sha1Hex(@NotNull File file) throws Exception {
        return IOHelper.sha1Hex(file);
    }
}
