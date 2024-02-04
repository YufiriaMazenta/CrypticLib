package crypticlib.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MapUtil {

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> T get(@NotNull Map<?, ?> map, @NotNull Object key, @NotNull Class<T> tClass) {
        Object object = map.get(key);
        if (object == null)
            return null;
        if (tClass.isInstance(object)) {
            return (T) object;
        } else {
            return null;
        }
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <T> List<T> getList(@NotNull Map<?, ?> map, @NotNull Object key) {
        Object object = map.get(key);
        if (object == null)
            return null;
        if (!(object instanceof List)) {
            return null;
        }
        return (List<T>) object;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public static <K, V> Map<K, V> getMap(@NotNull Map<?, ?> map, @NotNull Object key) {
        Object object = map.get(key);
        if (object == null)
            return null;
        if (!(object instanceof Map)) {
            return null;
        }
        return (Map<K, V>) object;
    }

    public static <K, V> HashMap<K, V> asHashMap(Collection<K> keys, Collection<V> values) {
        HashMap<K, V> map = new HashMap<>();
        setupMap(map, keys, values);
        return map;
    }

    public static <K, V> ConcurrentHashMap<K, V> asConcurrentHashMap(Collection<K> keys, Collection<V> values) {
        ConcurrentHashMap<K, V> map = new ConcurrentHashMap<>();
        setupMap(map, keys, values);
        return map;
    }

    public static <K, V> LinkedHashMap<K, V> asLinkedHashMap(Collection<K> keys, Collection<V> values) {
        LinkedHashMap<K, V> map = new LinkedHashMap<>();
        setupMap(map, keys, values);
        return map;
    }

    private static <K, V> void setupMap(Map<K, V> map, Collection<K> keys, Collection<V> values) {
        Iterator<K> keyIterator = keys.iterator();
        Iterator<V> valueIterator = values.iterator();
        while (keyIterator.hasNext() && valueIterator.hasNext()) {
            K key = keyIterator.next();
            V value = valueIterator.next();
            map.put(key, value);
        }
    }

}
