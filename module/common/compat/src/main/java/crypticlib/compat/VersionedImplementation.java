package crypticlib.compat;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

public final class VersionedImplementation<T> {

    private final @NotNull String minimumVersion;

    private final @NotNull Supplier<T> instanceSupplier;

    public VersionedImplementation(
        @NotNull String minimumVersion,
        @NotNull Supplier<T> instanceSupplier
    ) {
        this.minimumVersion = minimumVersion;
        this.instanceSupplier = instanceSupplier;
        Objects.requireNonNull(minimumVersion);
        Objects.requireNonNull(instanceSupplier);
    }

    public String minimumVersion() {
        return minimumVersion;
    }

    public T instance() {
        return instanceSupplier.get();
    }

}