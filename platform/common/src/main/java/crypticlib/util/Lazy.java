package crypticlib.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * 懒加载缓存,未初始化时通过创建代码初始化并缓存对象
 *
 * @param <T> 缓存的对象类型
 */
public interface Lazy<T> {

    /**
     * 获取缓存的对象,未初始化时通过创建代码初始化
     */
    T get();

    /**
     * 使对象失效,下次获取时通过创建代码重新创建
     */
    void invalidate();

    /**
     * 通过创建代码创建懒加载缓存
     */
    static <T> Lazy<T> of(@NotNull Supplier<T> supplier) {
        Objects.requireNonNull(supplier);
        return new SimpleLazy<>(supplier);
    }

    class SimpleLazy<T> implements Lazy<T> {

        private final Supplier<T> supplier;
        private volatile boolean initialized;
        private T value;

        public SimpleLazy(Supplier<T> supplier) {
            this.supplier = supplier;
        }


        @Override
        public T get() {
            if (!initialized) {
                synchronized (this) {
                    if (!initialized) {
                        value = supplier.get();
                        initialized = true;
                    }
                }
            }
            return value;
        }

        @Override
        public void invalidate() {
            synchronized (this) {
                initialized = false;
                value = null;
            }
        }

    }

}
