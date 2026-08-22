package crypticlib.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * 三元函数,接受三个参数,返回一个R类型对象
 */
@FunctionalInterface
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);

}
