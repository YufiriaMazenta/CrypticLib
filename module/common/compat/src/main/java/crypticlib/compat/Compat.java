package crypticlib.compat;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class Compat<T> {

    protected @NotNull Class<T> compatInterfaceClass;
    protected @NotNull VersionComparator versionComparator;
    protected final List<VersionedImplementation<T>> implementations = new ArrayList<>();

    public Compat(@NotNull Class<T> compatInterfaceClass, @NotNull VersionComparator versionComparator) {
        this.compatInterfaceClass = compatInterfaceClass;
        this.versionComparator = versionComparator;
        Objects.requireNonNull(compatInterfaceClass);
        Objects.requireNonNull(versionComparator);
    }

    public Compat<T> register(
        String minimumVersion,
        Supplier<T> implementationSupplier
    ) {
        implementations.add(
            new VersionedImplementation<>(
                minimumVersion,
                implementationSupplier
            )
        );

        implementations.sort(
            (a, b) -> versionComparator.compare(
                b.minimumVersion(),
                a.minimumVersion()
            )
        );
        return this;
    }

    public Optional<T> findImplementation(@NotNull String version) {
        for (VersionedImplementation<T> implementation : implementations) {
            if (versionComparator.compare(
                version,
                implementation.minimumVersion()
            ) >= 0) {
                return Optional.of(implementation.instance());
            }
        }

        return Optional.empty();
    }

}
