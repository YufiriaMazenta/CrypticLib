package crypticlib.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * 三元函数,接受三个参数,返回一个R类型对象
 */
public interface TriFunction<T, U, V, R> {

    R apply(T t, U u, V v);

    default <X> TriFunction<T, U, V, X> andThen(Function<? super R, ? extends X> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> after.apply(apply(t, u, v));
    }

}
