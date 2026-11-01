package crypticlib.env.dependency;

import crypticlib.CrypticLib;
import crypticlib.env.ClassAppender;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Maven 依赖下载器
 * 解析 POM、下载依赖、解析传递依赖
 */
public class DependencyDownloader extends AbstractXmlParser {

    //值用弱引用集合持有类加载器：插件卸载后其加载器可以被回收，
    //避免静态表一直钉住插件类加载器(以及它加载的全部类)
    private static final Map<Dependency, Set<ClassLoader>> injectedDependencies = new ConcurrentHashMap<>();

    private static final Set<Dependency> downloadedDependencies = new CopyOnWriteArraySet<>();

    /**
     * 正在解析中的依赖(用于阻断 POM 依赖环: A 依赖 B、B 又依赖 A)
     */
    private static final Set<Dependency> resolvingInProgress = ConcurrentHashMap.newKeySet();

    /**
     * 匹配 POM 中的 {@code <packaging>pom</packaging>} 声明
     */
    private static final Pattern POM_PACKAGING_PATTERN =
        Pattern.compile("<packaging>\\s*pom\\s*</packaging>", Pattern.CASE_INSENSITIVE);

    private final Set<Repository> repositories = new CopyOnWriteArraySet<>();
    private final Set<JarRelocation> relocation = new CopyOnWriteArraySet<>();
    private final File baseDir;
    private DependencyScope[] dependencyScopes = {DependencyScope.RUNTIME, DependencyScope.COMPILE};
    private boolean ignoreOptional = true;
    private boolean ignoreException = false;
    private boolean isTransitive = true;

    private static Set<ClassLoader> newClassLoaderSet() {
        return Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    }

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
                injectedDependencies.computeIfAbsent(dep, dependency -> newClassLoaderSet()).add(loader);
            } else {
                // JAR 不存在（可能是纯 POM 依赖），尝试下载一次
                try {
                    loadDependency(repositories, dep);
                } catch (IOException e) {
                    // 下载失败，标记为已处理避免无限递归
                    injectedDependencies.computeIfAbsent(dep, dependency -> newClassLoaderSet()).add(ClassAppender.getClassLoader());
                    continue;
                }
                // 下载成功后再次尝试注入（只重试一次）
                File retryFile = dep.findFile(baseDir, "jar");
                if (retryFile.exists()) {
                    if (!relocation.isEmpty()) {
                        retryFile = relocateJar(dep, retryFile);
                    }
                    ClassLoader loader = ClassAppender.addPath(retryFile.toPath());
                    injectedDependencies.computeIfAbsent(dep, dependency -> newClassLoaderSet()).add(loader);
                } else {
                    // JAR 仍然不存在，标记为已处理
                    injectedDependencies.computeIfAbsent(dep, dependency -> newClassLoaderSet()).add(ClassAppender.getClassLoader());
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

        //环检测: A 的 POM 依赖 B、B 的 POM 又依赖 A 时不能无限递归
        if (!resolvingInProgress.add(dependency)) {
            return Collections.singleton(dependency);
        }
        try {
            return doLoadDependency(repos, dependency);
        } finally {
            resolvingInProgress.remove(dependency);
        }
    }

    /**
     * 实际的下载与传递依赖解析
     */
    private Set<Dependency> doLoadDependency(@NotNull Collection<Repository> repos, @NotNull Dependency dependency) throws IOException {
        File pom = dependency.findFile(baseDir, "pom");
        File pom1 = new File(pom.getPath() + ".sha1");
        File jar = dependency.findFile(baseDir, "jar");
        File jar1 = new File(jar.getPath() + ".sha1");

        Set<Dependency> downloaded = ConcurrentHashMap.newKeySet();
        downloaded.add(dependency);

        // 检查是否已下载且完整: 有 .sha1 就校验, 没有则视为完整(仓库可能不提供校验文件)。
        // jar 缺失只有在 POM 声明 packaging=pom 时才算正常; 其它情况说明上次 jar 下载没有完成,
        // 必须重新下载, 否则一次失败会被永久当成"纯 POM 依赖", 该依赖再也不会被加载
        boolean jarReady = jar.exists() ? jarValid(jar, jar1) : declaresPomPackaging(pom);
        if (isPresentAndVerified(pom, pom1) && jarReady) {
            if (pom.exists()) {
                try (InputStream in = pom.toURI().toURL().openStream()) {
                    downloaded.addAll(loadDependencyFromInputStream(in));
                }
            }
            //传递依赖解析成功后才标记完成: 中途失败时若已标记, 后续调用会直接返回主构件, 传递依赖永久缺失
            downloadedDependencies.add(dependency);
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

        // 如果 POM 存在，解析传递依赖
        if (pom.exists()) {
            try (InputStream in = pom.toURI().toURL().openStream()) {
                downloaded.addAll(loadDependencyFromInputStream(in));
            }
        }

        //传递依赖解析成功后才是"已完成", 避免一次失败把传递依赖永久丢失
        downloadedDependencies.add(dependency);
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
                CrypticLib.warn("Failed to download transitive dependency " + dep, e);
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
            //POM 是完全不可信的远程内容(且其中的 <repositories> 也会参与下载), 必须关闭 DOCTYPE 与外部实体解析,
            //否则恶意 POM 可以读取本地文件并回显到日志/异常信息中, 或构造 billion-laughs
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://xml.org/sax/features/validation", false);
            factory.setExpandEntityReferences(false);
            factory.setXIncludeAware(false);
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
     * 判断 POM 是否声明了 {@code <packaging>pom</packaging>}。
     * <p>
     * 这类依赖(BOM/父 POM)本来就没有 jar; 其它依赖若缺少 jar, 说明上次下载没有完成,
     * 应按未完成处理并重新下载, 而不是当成纯 POM 依赖永久跳过。
     *
     * @param pom POM 文件
     * @return 是否声明为 pom 打包; 文件不可读时返回 false(按需要 jar 处理, 宁可重下)
     */
    static boolean declaresPomPackaging(@NotNull File pom) {
        if (!pom.isFile()) {
            return false;
        }
        try {
            String content = new String(Files.readAllBytes(pom.toPath()), StandardCharsets.UTF_8);
            return POM_PACKAGING_PATTERN.matcher(content).find();
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    /**
     * 判断文件已存在且可信: 有 .sha1 就校验, 没有则视为完整。
     * <p>
     * 注意: 这里的"没有 .sha1 视为完整"只用于决定"是否还要重新下载",
     * 不代表已经校验过完整性。
     */
    private boolean isPresentAndVerified(@NotNull File file, @NotNull File sha1File) {
        if (!file.exists()) {
            return false;
        }
        if (!sha1File.exists()) {
            return true;
        }
        return validation(file, sha1File);
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
        CrypticLib.info("Relocating " + dep + "...");
        File tempSource = File.createTempFile(file.getName(), ".jar");
        File tempOut = File.createTempFile(name, "_r2.jar", file.getParentFile());
        try {
            IOHelper.copyFile(file.toPath(), tempSource.toPath(), StandardCopyOption.REPLACE_EXISTING);
            new JarRelocator(tempSource, tempOut, rules).run();
            try {
                Files.move(tempOut.toPath(), rel.toPath(),
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(tempOut.toPath(), rel.toPath(),
                    StandardCopyOption.REPLACE_EXISTING);
            }
            CrypticLib.info("Relocated to " + rel.getName());
        } finally {
            tempSource.delete();
            if (tempOut.exists()) {
                tempOut.delete();
            }
        }
        return rel;
    }
}
