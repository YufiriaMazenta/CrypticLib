package crypticlib.function;

import java.util.Objects;
import java.util.function.Function;

/**
 * 三元函数,接受三个参数,返回一个R类型对象
 */
public interface TernaryFunction<T, U, V, R> {

    R apply(T t, U u, V v);

    default <X> TernaryFunction<T, U, V, X> andThen(Function<? super R, ? extends X> after) {
        Objects.requireNonNull(after);
        return (T t, U u, V v) -> after.apply(apply(t, u, v));
    }

}
