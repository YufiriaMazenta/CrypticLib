package crypticlib.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

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
        return collection.stream().allMatch(Objects::nonNull);
    }

    /**
     * 获取列表中所有符合条件对象的索引
     */
    public static <T> Set<Integer> getIndexes(List<T> list, Function<T, Boolean> filter) {
        Set<Integer> indexes = new HashSet<>();
        for (int i = 0; i < list.size(); i++) {
            if (filter.apply(list.get(i))) {
                indexes.add(i);
            }
        }
        return indexes;
    }

    public static <T> void addIf(List<T> list, T t, BiFunction<List<T>, T, Boolean> func) {
        if (func.apply(list, t)) {
            list.add(t);
        }
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
     * 添加或覆盖相等对象
     * 如果存在多个对象,将会一并覆盖
     */
    public static <T> void addOrReplace(List<T> list, T t) {
        Set<Integer> equalsIndexes = getIndexes(list, (it) -> Objects.equals(it, t));
        if (equalsIndexes.isEmpty()) {
            list.add(t);
        } else {
            equalsIndexes.forEach(index -> {
                list.set(index, t);
            });
        }
    }

    /**
     * 添加或覆盖已有符合条件对象
     * 如果存在多个对象,会一并覆盖
     */
    public static <T> void addOrReplace(List<T> list, T t, BiFunction<T, T, Boolean> filter) {
        Set<Integer> equalsIndexes = getIndexes(list, (it) -> filter.apply(it, t));
        if (equalsIndexes.isEmpty()) {
            list.add(t);
        } else {
            equalsIndexes.forEach(index -> {
                list.set(index, t);
            });
        }
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
