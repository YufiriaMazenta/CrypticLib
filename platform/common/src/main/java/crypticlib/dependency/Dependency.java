package crypticlib.dependency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.util.*;

/**
 * Maven 依赖模型
 */
public class Dependency extends AbstractXmlParser {

    public static final String REPOSITORY_MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2";
    public static final String REPOSITORY_MAVEN_CENTRAL_MIRROR_ALI = "https://maven.aliyun.com/repository/central";
    public static final String REPOSITORY_JITPACK = "https://jitpack.io";
    public static final String REPOSITORY_SONATYPE = "https://oss.sonatype.org/content/groups/public";
    private static final String LATEST_VERSION = "latest";

    private final String groupId;
    private final String artifactId;
    private final DependencyScope scope;
    private final List<String> repositories;
    private final String test;
    private final boolean transitive;
    private final List<JarRelocation> relocations;
    private String version;
    private boolean isExternal;

    private Dependency(Builder builder) {
        this.groupId = builder.groupId;
        this.artifactId = builder.artifactId;
        this.version = builder.version.contains("$") || builder.version.contains("[") || builder.version.contains("(")
            ? LATEST_VERSION : builder.version;
        this.scope = builder.scope;
        this.repositories = Collections.unmodifiableList(builder.repositories);
        this.test = builder.test;
        this.transitive = builder.transitive;
        this.relocations = Collections.unmodifiableList(builder.relocations);
    }

    public Dependency(@NotNull String groupId, @NotNull String artifactId, @NotNull String version, @NotNull DependencyScope scope) {
        this(builder(groupId, artifactId, version));
    }

    public Dependency(@NotNull Element node) throws ParseException {
        this(find("groupId", node), find("artifactId", node),
             find("version", node, LATEST_VERSION),
             DependencyScope.valueOf(find("scope", node, "runtime").toUpperCase()));
    }

    @NotNull
    public static Builder builder(@NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        return new Builder(groupId, artifactId, version);
    }

    @NotNull
    public static Builder builder(@NotNull String coordinate) {
        String[] parts = coordinate.split(":");
        if (parts.length < 3) {
            throw new IllegalArgumentException("Invalid coordinate: " + coordinate + ". Expected format: groupId:artifactId:version");
        }
        return new Builder(parts[0], parts[1], parts[2]);
    }

    @NotNull
    public URL getURL(@NotNull Repository repo, @NotNull String ext) throws MalformedURLException {
        String name = String.format("%s-%s.%s", getArtifactId(), getVersion(), ext);
        return new URL(String.format("%s/%s/%s/%s/%s", repo.getUrl(), getGroupId().replace('.', '/'), getArtifactId(), getVersion(), name));
    }

    public void checkVersion(@NotNull Collection<Repository> repositories, @NotNull File baseDir) throws IOException {
        if (getVersion() == null) {
            for (Repository repo : repositories) {
                try {
                    repo.getLatestVersion(this);
                    break;
                } catch (IOException ex) {
                    // 继续尝试下一个仓库
                }
            }
        }
    }

    @NotNull
    public File findFile(@NotNull File dir, @NotNull String ext) {
        if (getVersion() == null) {
            throw new IllegalStateException("Version is not resolved: " + this);
        }
        for (String part : getGroupId().split("\\.")) {
            dir = new File(dir, part);
        }
        dir = new File(dir, getArtifactId());
        dir = new File(dir, getVersion());
        return new File(dir, String.format("%s-%s.%s", getArtifactId(), getVersion(), ext));
    }

    public void setVersion(@NotNull String version) {
        if (!this.version.equals(LATEST_VERSION)) {
            throw new IllegalStateException("Version is already resolved");
        } else if (version.equals(LATEST_VERSION)) {
            throw new IllegalArgumentException("Cannot set version to the latest");
        }
        this.version = version;
    }

    @NotNull
    public String getGroupId() {
        return groupId;
    }

    @NotNull
    public String getArtifactId() {
        return artifactId;
    }

    @Nullable
    public String getVersion() {
        return version.equals(LATEST_VERSION) ? null : version;
    }

    @NotNull
    public List<String> getRepositories() {
        return repositories;
    }

    @Nullable
    public String getTest() {
        return test;
    }

    public boolean isTransitive() {
        return transitive;
    }

    @NotNull
    public List<JarRelocation> getRelocations() {
        return relocations;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setExternal(boolean external) {
        isExternal = external;
    }

    @NotNull
    public DependencyScope getScope() {
        return scope;
    }

    @Override
    @NotNull
    public String toString() {
        return String.format("%s:%s:%s", groupId, artifactId, version);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Dependency)) return false;
        Dependency that = (Dependency) o;
        return Objects.equals(getGroupId(), that.getGroupId()) &&
               Objects.equals(getArtifactId(), that.getArtifactId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getGroupId(), getArtifactId());
    }

    /**
     * Dependency 构建器
     */
    public static class Builder {
        private final String groupId;
        private final String artifactId;
        private String version;
        private DependencyScope scope = DependencyScope.RUNTIME;
        private final List<String> repositories = new ArrayList<>();
        private String test;
        private boolean transitive = true;
        private final List<JarRelocation> relocations = new ArrayList<>();

        public Builder(@NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
            this.groupId = groupId.replace("#", "").replace("%", ".");
            this.artifactId = artifactId.replace("#", "").replace("%", ".");
            this.version = version.replace("#", "").replace("%", ".");
        }

        @NotNull
        public Builder scope(@NotNull DependencyScope scope) {
            this.scope = scope;
            return this;
        }

        /**
         * 添加仓库，按添加顺序从上到下尝试
         *
         * @param repository 仓库 URL
         */
        @NotNull
        public Builder repository(@NotNull String repository) {
            this.repositories.add(repository);
            return this;
        }

        /**
         * 批量添加仓库，按添加顺序从上到下尝试
         *
         * @param repositories 仓库 URL 列表
         */
        @NotNull
        public Builder repositories(@NotNull List<String> repositories) {
            this.repositories.addAll(repositories);
            return this;
        }

        /**
         * 设置类存在性检测
         * @param test 类名，前缀 "!" 表示类不存在时才加载
         *         支持 % 代替 . 和 # 作为转义
         *         因为shadow的relocate会将字符串中的包名也重定向，所以这里可以写原始类名
         */
        @NotNull
        public Builder test(@Nullable String test) {
            if (test != null) {
                this.test = test.replace("#", "").replace("%", ".");
            }
            return this;
        }

        @NotNull
        public Builder transitive(boolean transitive) {
            this.transitive = transitive;
            return this;
        }

        @NotNull
        public Builder relocate(@NotNull String from, @NotNull String to) {
            this.relocations.add(new JarRelocation(from, to));
            return this;
        }

        @NotNull
        public Builder relocations(@NotNull List<JarRelocation> relocations) {
            this.relocations.addAll(relocations);
            return this;
        }

        @NotNull
        public Builder external(boolean external) {
            // 会在 build 后设置
            return this;
        }

        @NotNull
        public Dependency build() {
            return new Dependency(this);
        }
    }
}
