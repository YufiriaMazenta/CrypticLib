package crypticlib.hook;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class HookManager {

    private static final Map<Class<?>, AbstractHook<?>> HOOKS = new ConcurrentHashMap<>();

    private HookManager() {
        throw new UnsupportedOperationException();
    }

    public static void register(AbstractHook<?> hook) {
        if (HOOKS.putIfAbsent(
            hook.hookClass,
            hook
        ) != null) {
            throw new IllegalStateException(
                "Hook class already registered: "
                    + hook.hookClass.getName()
            );
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Optional<AbstractHook<T>> getHook(Class<T> hookClass) {
        return Optional.ofNullable(
            (AbstractHook<T>) HOOKS.get(hookClass)
        );
    }

    public static <T> Optional<T> getImplementation(Class<T> hookClass, String version) {
        return getHook(hookClass)
            .flatMap(
                hook -> hook.findImplementation(version)
            );
    }

}
