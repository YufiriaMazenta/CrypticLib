package crypticlib.compat;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CompatManager {

    private static final Map<Class<?>, AbstractCompat<?>> COMPAT_MAP = new ConcurrentHashMap<>();

    private CompatManager() {
        throw new UnsupportedOperationException();
    }

    public static void register(AbstractCompat<?> compat) {
        if (COMPAT_MAP.putIfAbsent(
            compat.compatInterfaceClass,
            compat
        ) != null) {
            throw new IllegalStateException(
                "Compatibility already registered: "
                    + compat.compatInterfaceClass.getName()
            );
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<AbstractCompat<T>> getCompat(Class<T> hookClass) {
        return Optional.ofNullable(
            (AbstractCompat<T>) COMPAT_MAP.get(hookClass)
        );
    }

    public static <T> Optional<T> getImplementation(Class<T> hookClass, String version) {
        return getCompat(hookClass)
            .flatMap(
                hook -> hook.findImplementation(version)
            );
    }

}
