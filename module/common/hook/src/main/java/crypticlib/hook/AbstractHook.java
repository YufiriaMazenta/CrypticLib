package crypticlib.hook;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public abstract class AbstractHook<T> {

    protected @NotNull Class<T> hookClass;
    protected @NotNull VersionComparator versionComparator;
    protected final List<VersionedImplementation<T>> implementations = new ArrayList<>();

    public AbstractHook(@NotNull Class<T> hookClass, @NotNull VersionComparator versionComparator) {
        this.hookClass = hookClass;
        this.versionComparator = versionComparator;
        Objects.requireNonNull(hookClass);
        Objects.requireNonNull(versionComparator);
    }

    public AbstractHook<T> register(
        String minimumVersion,
        T implementation
    ) {
        implementations.add(
            new VersionedImplementation<>(
                minimumVersion,
                implementation
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
