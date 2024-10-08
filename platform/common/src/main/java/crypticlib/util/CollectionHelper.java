package crypticlib.util;

import java.util.Collection;
import java.util.Objects;

public class CollectionHelper {

    public static <T> boolean isAllNull(Collection<T> collection) {
        return collection.stream().allMatch(Objects::isNull);
    }

    public static <T> boolean isAllNotNull(Collection<T> collection) {
        return collection.stream().anyMatch(Objects::nonNull);
    }

}
