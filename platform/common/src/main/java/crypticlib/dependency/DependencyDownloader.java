package crypticlib.dependency;

import crypticlib.util.IOHelper;
import me.lucko.jarrelocator.JarRelocator;
import me.lucko.jarrelocator.Relocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
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
            // 注入前校验 JAR 完整性, 损坏(如上次下载被强杀留下的残缺文件)则删除以触发重新下载
            if (file.exists()) {
                File fileSha1 = new File(file.getPath() + ".sha1");
                if (fileSha1.exists() && !validation(file, fileSha1)) {
                    file.delete();
                    fileSha1.delete();
                }
            }
            if (file.exists()) {
                if (!relocation.isEmpty()) {
                    file = relocateJar(dep, file);
                }
                ClassLoader loader = ClassAppender.addPath(file.toPath());
                injectedDependencies.computeIfAbsent(dep, dependency -> ConcurrentHashMap.newKeySet()).add(loader);
            } else {
                // JAR 不存在（可能是纯 POM 依赖），尝试下载一次
                try {
                    loadDependency(repositories, dep);
                } catch (IOException e) {
                    // 下载失败，标记为已处理避免无限递归
                    injectedDependencies.computeIfAbsent(dep, dependency -> ConcurrentHashMap.newKeySet()).add(ClassAppender.getClassLoader());
                    continue;
                }
                // 下载成功后再次尝试注入（只重试一次）
                File retryFile = dep.findFile(baseDir, "jar");
                if (retryFile.exists()) {
                    if (!relocation.isEmpty()) {
                        retryFile = relocateJar(dep, retryFile);
                    }
                    ClassLoader loader = ClassAppender.addPath(retryFile.toPath());
                    injectedDependencies.computeIfAbsent(dep, dependency -> ConcurrentHashMap.newKeySet()).add(loader);
                } else {
                    // JAR 仍然不存在，标记为已处理
                    injectedDependencies.computeIfAbsent(dep, dependency -> ConcurrentHashMap.newKeySet()).add(ClassAppender.getClassLoader());
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

        Set<Dependency> downloaded = ConcurrentHashMap.newKeySet();
        downloaded.add(dependency);

        // 检查是否已下载且完整（pom 与 jar 都需通过 sha1 校验; jar 不存在视为纯 POM 依赖）
        if (validation(pom, pom1) && jarValid(jar, jar1)) {
            downloadedDependencies.add(dependency);
            if (pom.exists()) {
                try (InputStream in = pom.toURI().toURL().openStream()) {
                    downloaded.addAll(loadDependencyFromInputStream(in));
                }
            }
            return downloaded;
        }

        pom.getParentFile().mkdirs();

        // 合并传入的 repos 参数与实例仓库, 保证 POM <repositories> 声明的自定义仓库参与实际下载
        Set<Repository> downloadRepos = new LinkedHashSet<>(repos);
        downloadRepos.addAll(repositories);

        IOException lastError = null;
        boolean pomDownloaded = false;
        for (Repository repo : downloadRepos) {
            try {
                repo.downloadFile(dependency, pom);
                // JAR 可能不存在（纯 POM 依赖），忽略 JAR 下载失败
                try {
                    repo.downloadFile(dependency, jar);
                } catch (IOException e) {
                    // 纯 POM 依赖没有 JAR，这是正常的
                }
                // 下载后立即用 .sha1 校验完整性; sha1 缺失(仓库不提供)时降级为跳过校验
                if (pom1.exists() && !validation(pom, pom1)) {
                    throw new IOException("POM checksum mismatch for " + dependency);
                }
                if (jar.exists() && jar1.exists() && !validation(jar, jar1)) {
                    throw new IOException("JAR checksum mismatch for " + dependency);
                }
                pomDownloaded = true;
                lastError = null;
                break;
            } catch (Exception ex) {
                // 清理该仓库可能留下的不完整/损坏文件后再尝试下一个仓库
                deleteQuietly(pom);
                deleteQuietly(pom1);
                deleteQuietly(jar);
                deleteQuietly(jar1);
                lastError = new IOException(String.format("Unable to find download for %s (%s)", dependency, repo.url()), ex);
            }
        }

        // 一个仓库都没成功(含循环从未执行的情形)时抛异常, 避免"什么都没下载却标记成功"的静默失败
        if (!pomDownloaded) {
            throw lastError != null ? lastError
                : new IOException("No repository available to download " + dependency);
        }

        downloadedDependencies.add(dependency);

        // 如果 POM 存在，解析传递依赖
        if (pom.exists()) {
            try (InputStream in = pom.toURI().toURL().openStream()) {
                downloaded.addAll(loadDependencyFromInputStream(in));
            }
        }

        return downloaded;
    }

    /**
     * 批量下载依赖（传递依赖下载失败时会跳过而不是中断）
     */
    public Set<Dependency> loadDependency(@NotNull List<Repository> repos, @NotNull List<Dependency> dependencies) throws IOException {
        createBaseDir();
        Set<Dependency> downloaded = ConcurrentHashMap.newKeySet();
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

        // 解析 <repositories>
        try {
            for (int i = 0; i < nodes.getLength(); ++i) {
                Node node = nodes.item(i);
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
        // 只取根元素下 <dependencies> 的直接子 <dependency>, 避免命中
        // <dependencyManagement>、<build><plugins>、<profiles> 等处的 <dependency> 节点
        if (isTransitive) {
            Element dependenciesElement = null;
            NodeList rootChildren = pom.getDocumentElement().getChildNodes();
            for (int i = 0; i < rootChildren.getLength(); ++i) {
                Node node = rootChildren.item(i);
                if (node.getNodeName().equals("dependencies")) {
                    dependenciesElement = (Element) node;
                    break;
                }
            }
            if (dependenciesElement != null) {
                try {
                    NodeList depNodes = dependenciesElement.getChildNodes();
                    for (int i = 0; i < depNodes.getLength(); ++i) {
                        Node depNode = depNodes.item(i);
                        if (!(depNode instanceof Element) || !depNode.getNodeName().equals("dependency")) {
                            continue;
                        }
                        Element depElement = (Element) depNode;
                        if (ignoreOptional && find("optional", depElement, "false").equals("true")) {
                            continue;
                        }
                        Dependency dep = new Dependency(depElement);
                        if (scopeSet.contains(dep.scope())) {
                            dependencies.add(dep);
                        }
                    }
                } catch (ParseException ex) {
                    if (!ignoreException) {
                        throw new IOException("Unable to parse dependencies", ex);
                    }
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
    public File baseDir() {
        return baseDir;
    }

    @NotNull
    public DependencyScope[] dependencyScopes() {
        return dependencyScopes;
    }

    @NotNull
    public DependencyDownloader setDependencyScopes(@NotNull DependencyScope[] dependencyScopes) {
        this.dependencyScopes = dependencyScopes;
        return this;
    }

    @NotNull
    public Map<Dependency, Set<ClassLoader>> injectedDependencies() {
        return injectedDependencies;
    }

    @NotNull
    public Set<Repository> repositories() {
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
    public Set<JarRelocation> relocation() {
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

    /**
     * 校验 JAR 完整性: JAR 不存在时视为纯 POM 依赖(合法); 存在但无 .sha1 时降级为跳过校验。
     */
    private boolean jarValid(@NotNull File jar, @NotNull File sha1File) {
        if (!jar.exists()) {
            return true;
        }
        if (!sha1File.exists()) {
            return true;
        }
        return validation(jar, sha1File);
    }

    private void deleteQuietly(@NotNull File file) {
        if (file.exists()) {
            file.delete();
        }
    }

    private boolean validation(@NotNull File file, @NotNull File sha1File) {
        return IOHelper.validateSha1(file, sha1File);
    }

    @NotNull
    private String sha1Hex(@NotNull File file) throws Exception {
        return IOHelper.sha1Hex(file);
    }

    @NotNull
    private File copyFile(@NotNull File source, @NotNull File dest) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return dest;
    }

    /**
     * 对 JAR 执行重定位。
     * 先写入与目标同目录的临时文件, 完成后原子 rename 到目标, 避免中途失败留下非空但损坏的产物;
     * 临时源拷贝在 finally 中删除, 避免残留在系统临时目录。
     */
    @NotNull
    private File relocateJar(@NotNull Dependency dep, @NotNull File file) throws IOException {
        String name = file.getName().substring(0, file.getName().lastIndexOf('.'));
        File rel = new File(file.getParentFile(), name + "_r2_" + Math.abs(relocation.hashCode()) + ".jar");
        if (rel.exists() && rel.length() > 0) {
            return rel;
        }
        List<Relocation> rules = relocation.stream()
            .map(JarRelocation::toRelocation)
            .collect(Collectors.toList());
        IOHelper.info("Relocating " + dep + "...");
        File tempSource = File.createTempFile(file.getName(), ".jar");
        File tempOut = File.createTempFile(name, "_r2.jar", file.getParentFile());
        try {
            copyFile(file, tempSource);
            new JarRelocator(tempSource, tempOut, rules).run();
            try {
                Files.move(tempOut.toPath(), rel.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException e) {
                Files.move(tempOut.toPath(), rel.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            IOHelper.info("Relocated to " + rel.getName());
        } finally {
            tempSource.delete();
            if (tempOut.exists()) {
                tempOut.delete();
            }
        }
        return rel;
    }
}
