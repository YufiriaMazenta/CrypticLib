package crypticlib.compat;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public enum CompatManager {

    INSTANCE;

    private static final Map<Class<?>, Compat<?>> COMPAT_MAP = new ConcurrentHashMap<>();

    public static void register(Compat<?> compat) {
        if (COMPAT_MAP.putIfAbsent(
            compat.compatInterfaceClass,
            compat
        ) != null) {
            throw new IllegalStateException(
                "Compat already registered: "
                    + compat.compatInterfaceClass.getName()
            );
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<Compat<T>> getCompat(Class<T> hookClass) {
        return Optional.ofNullable(
            (Compat<T>) COMPAT_MAP.get(hookClass)
        );
    }

    public static <T> Optional<T> findImplementation(Class<T> hookClass, String version) {
        return getCompat(hookClass)
            .flatMap(
                hook -> hook.findImplementation(version)
            );
    }

}
