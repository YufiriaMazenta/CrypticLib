package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PomDependency {

    private final String groupId;
    private final String artifactId;
    private final String version;
    private final String scope;
    private final boolean optional;

    public PomDependency(@NotNull String groupId, @NotNull String artifactId, @Nullable String version, @Nullable String scope, boolean optional) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope == null ? "compile" : scope;
        this.optional = optional;
    }

    @NotNull
    public String groupId() {
        return groupId;
    }

    @NotNull
    public String artifactId() {
        return artifactId;
    }

    @Nullable
    public String version() {
        return version;
    }

    @NotNull
    public String scope() {
        return scope;
    }

    public boolean optional() {
        return optional;
    }

    @NotNull
    public String toKey() {
        return groupId + ":" + artifactId;
    }

    @NotNull
    public String toCoord() {
        return groupId + ":" + artifactId + ":" + version;
    }

}
