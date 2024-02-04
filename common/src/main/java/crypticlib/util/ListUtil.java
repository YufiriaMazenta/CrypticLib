package crypticlib.util;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ListUtil {

    @SuppressWarnings("unchecked")
    public <T> T get(List<?> list, int index, Class<T> tClass) {
        Object object = list.get(index);
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
    public <T> List<T> getList(List<?> list, int index) {
        Object object = list.get(index);
        if (!(object instanceof List))
            return null;
        return (List<T>) object;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    public <K, V> Map<K, V> getMap(List<?> list, int index) {
        Object object = list.get(index);
        if (!(object instanceof List))
            return null;
        return (Map<K, V>) object;
    }

    @SafeVarargs
    public static <T> ArrayList<T> asArrayList(T...t) {
        return new ArrayList<>(Arrays.asList(t));
    }

    @SafeVarargs
    public static <T> CopyOnWriteArrayList<T> asCopyOnWriteArrayList(T... t) {
        return new CopyOnWriteArrayList<>(Arrays.asList(t));
    }

}
