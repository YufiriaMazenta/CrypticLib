package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Library {

    public static final String REPOSITORY_MAVEN_CENTRAL = "https://repo.maven.apache.org/maven2/";
    public static final String REPOSITORY_MAVEN_CENTRAL_MIRROR_ALI = "https://maven.aliyun.com/repository/public";
    public static final String REPOSITORY_JITPACK = "https://jitpack.io";
    public static final String REPOSITORY_SONATYPE = "https://oss.sonatype.org/content/groups/public/";

    private final @NotNull String repository;
    private final @NotNull String dependency;
    private final List<Relocation> relocations;

    public Library(@NotNull String dependency) {
        this(REPOSITORY_MAVEN_CENTRAL, dependency);
    }

    public Library(@NotNull String repository, @NotNull String dependency) {
        this(repository, dependency, new Relocation[0]);
    }

    public Library(@NotNull String repository, @NotNull String dependency, @NotNull Relocation... relocations) {
        this.repository = repository;
        // 支持在依赖字符串中用 # 避免 Shadow relocate 误伤，构造时去掉
        this.dependency = dependency.replace("#", "");
        this.relocations = Collections.unmodifiableList(Arrays.asList(relocations));
    }

    @NotNull
    public String repository() {
        return repository;
    }

    @NotNull
    public String dependency() {
        return dependency;
    }

    @NotNull
    public List<Relocation> relocations() {
        return relocations;
    }

    @NotNull
    public String groupId() {
        return dependency.split(":")[0];
    }

    @NotNull
    public String artifactId() {
        return dependency.split(":")[1];
    }

    @NotNull
    public String version() {
        return dependency.split(":")[2];
    }

    /**
     * 将 relocations 列表转换为 Map 格式（兼容旧代码）
     * @return Map 格式的 relocation 规则
     */
    @NotNull
    public Map<String, String> relocateMap() {
        if (relocations.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (Relocation r : relocations) {
            map.put(r.from(), r.to());
        }
        return Collections.unmodifiableMap(map);
    }

}
