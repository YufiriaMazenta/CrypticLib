package crypticlib.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;

public class CollectionHelper {

    /**
     * 判断一个集合里是否全是null
     */
    public static <T> boolean isAllNull(Collection<T> collection) {
        return collection.stream().allMatch(Objects::isNull);
    }

    /**
     * 判断一个集合里是否全部都不是null
     */
    public static <T> boolean isAllNotNull(Collection<T> collection) {
        return collection.stream().anyMatch(Objects::nonNull);
    }

    /**
     * 获取列表中所有符合条件对象的索引
     */
    public static <T> Set<Integer> getIndexes(List<T> list, Function<T, Boolean> equalsFunc) {
        Set<Integer> indexes = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            if (equalsFunc.apply(list.get(i))) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    /**
     * 如果不包含,则添加到list里
     */
    public static <T> void addIfNotContains(List<T> list, T t) {
        if (list.contains(t)) {
            return;
        }
        list.add(t);
    }

    /**
     * 如果不包含,则添加到list里
     */
    public static <T> void addIfNotContains(List<T> list, T t, BiFunction<T, T, Boolean> equalsFunc) {
        if (contains(list, t, equalsFunc)) {
            return;
        }
        list.add(t);
    }

    /**
     * 通过自定义equals方法判断是否包含
     */
    public static <T> boolean contains(List<T> list, T t, BiFunction<T, T, Boolean> equalsFunc) {
        for (T t1 : list) {
            if (equalsFunc.apply(t1, t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 去除重复元素
     */
    public static <T> List<T> removeDuplication(List<T> list) {
        TreeSet<T> set = new TreeSet<>(list);
        list.clear();
        list.addAll(set);
        return list;
    }

}
