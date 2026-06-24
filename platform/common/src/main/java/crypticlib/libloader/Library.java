package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class Library {

    private final String repository;
    private final String dependency;
    private final Map<String, String> relocate;

    public Library(@NotNull String repository, @NotNull String dependency) {
        this(repository, dependency, null);
    }

    public Library(@NotNull String repository, @NotNull String dependency, @Nullable Map<String, String> relocate) {
        this.repository = repository;
        this.dependency = dependency;
        this.relocate = relocate == null ? Collections.emptyMap() : Collections.unmodifiableMap(new LinkedHashMap<>(relocate));
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
    public Map<String, String> relocate() {
        return relocate;
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

}
