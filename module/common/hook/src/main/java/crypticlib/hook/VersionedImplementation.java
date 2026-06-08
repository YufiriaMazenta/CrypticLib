package crypticlib.hook;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public final class VersionedImplementation<T> {

    private final @NotNull String minimumVersion;

    private final @NotNull T instance;

    public VersionedImplementation(
        @NotNull String minimumVersion,
        @NotNull T instance
    ) {
        this.minimumVersion = minimumVersion;
        this.instance = instance;
        Objects.requireNonNull(minimumVersion);
        Objects.requireNonNull(instance);
    }

    public String minimumVersion() {
        return minimumVersion;
    }

    public T instance() {
        return instance;
    }
}