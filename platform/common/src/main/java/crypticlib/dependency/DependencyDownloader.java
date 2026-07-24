package crypticlib.dependency;

import crypticlib.util.IOHelper;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * Maven 依赖下载器
 * 解析 POM、下载依赖、解析传递依赖
 */
public class DependencyDownloader extends AbstractXmlParser {

    private static final Map<Dependency, Set<ClassLoader>> injectedDependencies = new ConcurrentHashMap<>();
    private static final Set<Dependency> downloadedDependencies = new CopyOnWriteArraySet<>();

    private final Set<Repository> repositories = new CopyOnWriteArraySet<>();
    private final Set<JarRelocation> relocation = new CopyOnWriteArraySet<>();
    private final File baseDir;
    private DependencyScope[] dependencyScopes = {DependencyScope.RUNTIME, DependencyScope.COMPILE};
    private boolean ignoreOptional = true;
    private boolean ignoreException = false;
    private boolean isTransitive = true;

    public DependencyDownloader(@Nullable File baseDir, @Nullable List<JarRelocation> relocation) {
        this.baseDir = baseDir;
        if (relocation != null) {
            for (JarRelocation rel : relocation) {
                if (rel != null) {
                    this.relocation.add(rel);
                }
            }
        }
    }

    /**
     * 将依赖注入到 ClassLoader
     */
    public void injectClasspath(@NotNull Set<Dependency> dependencies) throws Throwable {
        for (Dependency dep : dependencies) {
            Set<ClassLoader> injectedDependencyClassLoaders = injectedDependencies.get(dep);
            if (injectedDependencyClassLoaders != null && injectedDependencyClassLoaders.contains(ClassAppender.getClassLoader())) {
                continue;
            }

            File file = dep.findFile(baseDir, "jar");
            if (file.exists()) {
                if (!relocation.isEmpty()) {
                    String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
                    File rel = new File(file.getParentFile(), name + "_r2_" + Math.abs(relocation.hashCode()) + ".jar");
                    if (!rel.exists() || rel.length() == 0) {
                        List<Relocation> rules = relocation.stream()
                            .map(JarRelocation::toRelocation)
                            .collect(Collectors.toList());
                        IOHelper.info("Relocating " + dep + "...");
                        File tempSourceFile = copyFile(file, File.createTempFile(file.getName(), ".jar"));
                        new JarRelocator(tempSourceFile, rel, rules).run();
                        IOHelper.info("Relocated to " + rel.getName());
                    }
                    file = rel;
                }
                ClassLoader loader = ClassAppender.addPath(file.toPath());
                injectedDependencies.computeIfAbsent(dep, dependency -> new HashSet<>()).add(loader);
            } else {
                // JAR 不存在（可能是纯 POM 依赖），尝试下载一次
                try {
                    loadDependency(repositories, dep);
                } catch (IOException e) {
                    // 下载失败，标记为已处理避免无限递归
                    injectedDependencies.computeIfAbsent(dep, dependency -> new HashSet<>()).add(ClassAppender.getClassLoader());
                    continue;
                }
                // 下载成功后再次尝试注入（只重试一次）
                File retryFile = dep.findFile(baseDir, "jar");
                if (retryFile.exists()) {
                    if (!relocation.isEmpty()) {
                        String name = retryFile.getName().substring(0, retryFile.getName().lastIndexOf('.'));
                        File rel = new File(retryFile.getParentFile(), name + "_r2_" + Math.abs(relocation.hashCode()) + ".jar");
                        if (!rel.exists() || rel.length() == 0) {
                            List<Relocation> rules = relocation.stream()
                                .map(JarRelocation::toRelocation)
                                .collect(Collectors.toList());
                            IOHelper.info("Relocating " + dep + "...");
                            File tempSourceFile = copyFile(retryFile, File.createTempFile(retryFile.getName(), ".jar"));
                            new JarRelocator(tempSourceFile, rel, rules).run();
                            IOHelper.info("Relocated to " + rel.getName());
                        }
                        retryFile = rel;
                    }
                    ClassLoader loader = ClassAppender.addPath(retryFile.toPath());
                    injectedDependencies.computeIfAbsent(dep, dependency -> new HashSet<>()).add(loader);
                } else {
                    // JAR 仍然不存在，标记为已处理
                    injectedDependencies.computeIfAbsent(dep, dependency -> new HashSet<>()).add(ClassAppender.getClassLoader());
                }
            }
        }
    }

    /**
     * 下载依赖及其传递依赖
     */
    public Set<Dependency> loadDependency(@NotNull Collection<Repository> repos, @NotNull Dependency dependency) throws IOException {
        if (repos.isEmpty()) {
            throw new IllegalArgumentException("No repositories specified");
        }

        dependency.checkVersion(repos, baseDir);

        if (downloadedDependencies.contains(dependency)) {
            return Collections.singleton(dependency);
        }

        File pom = dependency.findFile(baseDir, "pom");
        File pom1 = new File(pom.getPath() + ".sha1");
        File jar = dependency.findFile(baseDir, "jar");
        File jar1 = new File(jar.getPath() + ".sha1");

        Set<Dependency> downloaded = new HashSet<>();
        downloaded.add(dependency);

        // 检查是否已下载且完整
        if (validation(pom, pom1)) {
            downloadedDependencies.add(dependency);
            if (pom.exists()) {
                downloaded.addAll(loadDependencyFromInputStream(pom.toURI().toURL().openStream()));
            }
            return downloaded;
        }

        pom.getParentFile().mkdirs();

        IOException lastError = null;
        boolean pomDownloaded = false;
        for (Repository repo : repositories) {
            try {
                repo.downloadFile(dependency, pom);
                pomDownloaded = true;
                // JAR 可能不存在（纯 POM 依赖），忽略 JAR 下载失败
                try {
                    repo.downloadFile(dependency, jar);
                } catch (IOException e) {
                    // 纯 POM 依赖没有 JAR，这是正常的
                }
                lastError = null;
                break;
            } catch (Exception ex) {
                lastError = new IOException(String.format("Unable to find download for %s (%s)", dependency, repo.getUrl()), ex);
            }
        }

        if (!pomDownloaded && lastError != null) {
            throw lastError;
        }

        downloadedDependencies.add(dependency);

        // 如果 POM 存在，解析传递依赖
        if (pom.exists()) {
            downloaded.addAll(loadDependencyFromInputStream(pom.toURI().toURL().openStream()));
        }

        return downloaded;
    }

    /**
     * 批量下载依赖（传递依赖下载失败时会跳过而不是中断）
     */
    public Set<Dependency> loadDependency(@NotNull List<Repository> repos, @NotNull List<Dependency> dependencies) throws IOException {
        createBaseDir();
        Set<Dependency> downloaded = new HashSet<>();
        for (Dependency dep : dependencies) {
            try {
                downloaded.addAll(loadDependency(repos, dep));
            } catch (IOException e) {
                // 传递依赖下载失败时跳过，不影响主依赖加载
                IOHelper.info("Warning: Failed to download transitive dependency " + dep);
            }
        }
        return downloaded;
    }

    /**
     * 从 POM 文档解析依赖
     */
    public Set<Dependency> loadDependencyFromPom(@NotNull Document pom) throws IOException {
        List<Dependency> dependencies = new ArrayList<>();
        Set<DependencyScope> scopeSet = new HashSet<>(Arrays.asList(dependencyScopes));
        NodeList nodes = pom.getDocumentElement().getChildNodes();
        List<Repository> repos = new ArrayList<>(repositories);
        if (repos.isEmpty()) {
            repos.add(new Repository());
        }

        // 解析 <repositories>
        try {
            for (int i = 0; i < nodes.getLength(); ++i) {
                org.w3c.dom.Node node = nodes.item(i);
                if (node.getNodeName().equals("repositories")) {
                    nodes = ((Element) node).getElementsByTagName("repository");
                    for (i = 0; i < nodes.getLength(); ++i) {
                        Element e = (Element) nodes.item(i);
                        repos.add(new Repository(e));
                    }
                    break;
                }
            }
        } catch (ParseException ex) {
            throw new IOException("Unable to parse repositories", ex);
        }

        // 解析 <dependencies>
        if (isTransitive) {
            nodes = pom.getElementsByTagName("dependency");
            try {
                for (int i = 0; i < nodes.getLength(); ++i) {
                    if (ignoreOptional && find("optional", (Element) nodes.item(i), "false").equals("true")) {
                        continue;
                    }
                    Dependency dep = new Dependency((Element) nodes.item(i));
                    if (scopeSet.contains(dep.getScope())) {
                        dependencies.add(dep);
                    }
                }
            } catch (ParseException ex) {
                if (!ignoreException) {
                    throw new IOException("Unable to parse dependencies", ex);
                }
            }
        }

        return loadDependency(repos, dependencies);
    }

    /**
     * 从输入流解析 POM
     */
    public Set<Dependency> loadDependencyFromInputStream(@NotNull InputStream pom) throws IOException {
        return loadDependencyFromInputStream(pom, dependencyScopes);
    }

    /**
     * 从输入流解析 POM（指定范围）
     */
    public Set<Dependency> loadDependencyFromInputStream(@NotNull InputStream pom, @NotNull DependencyScope... scopes) throws IOException {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document xml = builder.parse(pom);
            return loadDependencyFromPom(xml);
        } catch (Exception ex) {
            throw new IOException("Unable to parse pom.xml", ex);
        }
    }

    public void addRepository(@NotNull Repository repository) {
        repositories.add(repository);
    }

    @NotNull
    public File getBaseDir() {
        return baseDir;
    }

    @NotNull
    public DependencyScope[] getDependencyScopes() {
        return dependencyScopes;
    }

    @NotNull
    public DependencyDownloader setDependencyScopes(@NotNull DependencyScope[] dependencyScopes) {
        this.dependencyScopes = dependencyScopes;
        return this;
    }

    @NotNull
    public Map<Dependency, Set<ClassLoader>> getInjectedDependencies() {
        return injectedDependencies;
    }

    @NotNull
    public Set<Repository> getRepositories() {
        return repositories;
    }

    public boolean isIgnoreOptional() {
        return ignoreOptional;
    }

    @NotNull
    public DependencyDownloader setIgnoreOptional(boolean ignoreOptional) {
        this.ignoreOptional = ignoreOptional;
        return this;
    }

    @NotNull
    public DependencyDownloader setIgnoreException(boolean ignoreException) {
        this.ignoreException = ignoreException;
        return this;
    }

    @NotNull
    public Set<JarRelocation> getRelocation() {
        return relocation;
    }

    public boolean isTransitive() {
        return isTransitive;
    }

    public void setTransitive(boolean transitive) {
        isTransitive = transitive;
    }

    private void createBaseDir() {
        if (baseDir != null) {
            baseDir.mkdirs();
        }
    }

    private boolean validation(@NotNull File file, @NotNull File sha1File) {
        if (!file.exists() || !sha1File.exists()) {
            return false;
        }
        try {
            String expected = new String(Files.readAllBytes(sha1File.toPath())).trim().split("\\s+")[0];
            String actual = sha1Hex(file);
            return expected.equalsIgnoreCase(actual);
        } catch (Exception e) {
            return false;
        }
    }

    @NotNull
    private String sha1Hex(@NotNull File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream is = Files.newInputStream(file.toPath())) {
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

    @NotNull
    private File copyFile(@NotNull File source, @NotNull File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return dest;
    }
}
