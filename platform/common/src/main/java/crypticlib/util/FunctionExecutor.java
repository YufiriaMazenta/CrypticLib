package crypticlib.util;

import java.util.concurrent.Callable;
import java.util.function.*;

/**
 * 函数式接口调用相关工具类
 */
public class FunctionExecutor {

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static long execute(Runnable task) {
        long startTime = System.nanoTime();
        task.run();
        return nanoTime2Millis(System.nanoTime() - startTime);
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <T> long execute(Consumer<T> task, T t) {
        long startTime = System.nanoTime();
        task.accept(t);
        return nanoTime2Millis(System.nanoTime() - startTime);
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <T1, T2> long execute(BiConsumer<T1, T2> task, T1 t1, T2 t2) {
        long startTime = System.nanoTime();
        task.accept(t1, t2);
        return nanoTime2Millis(System.nanoTime() - startTime);
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <T, R> ExecutionResult<R> execute(Function<T, R> task, T t) {
        long startTime = System.nanoTime();
        R result = task.apply(t);
        return new ExecutionResult<>(result, nanoTime2Millis(System.nanoTime() - startTime));
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <T1, T2, R> ExecutionResult<R> execute(BiFunction<T1, T2, R> task, T1 t1, T2 t2) {
        long startTime = System.nanoTime();
        R result = task.apply(t1, t2);
        return new ExecutionResult<>(result, nanoTime2Millis(System.nanoTime() - startTime));
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <R> ExecutionResult<R> execute(Supplier<R> task) {
        long startTime = System.nanoTime();
        R result = task.get();
        return new ExecutionResult<>(result, nanoTime2Millis(System.nanoTime() - startTime));
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <T> ExecutionResult<Boolean> execute(Predicate<T> task, T t) {
        long startTime = System.nanoTime();
        boolean result = task.test(t);
        return new ExecutionResult<>(result, nanoTime2Millis(System.nanoTime() - startTime));
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <T1, T2> ExecutionResult<Boolean> execute(BiPredicate<T1, T2> task, T1 t1, T2 t2) {
        long startTime = System.nanoTime();
        boolean result = task.test(t1, t2);
        return new ExecutionResult<>(result, nanoTime2Millis(System.nanoTime() - startTime));
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <T> ExecutionResult<T> execute(BinaryOperator<T> task, T t1, T t2) {
        long startTime = System.nanoTime();
        T result = task.apply(t1, t2);
        return new ExecutionResult<>(result, nanoTime2Millis(System.nanoTime() - startTime));
    }

    /**
     * 执行任务，并返回所用时间(毫秒)
     *
     * @return 执行任务所用的时间(毫秒)
     */
    public static <R> ExecutionResult<R> execute(Callable<R> task) throws Exception {
        long startTime = System.nanoTime();
        R result = task.call();
        return new ExecutionResult<>(result, nanoTime2Millis(System.nanoTime() - startTime));
    }

    private static long nanoTime2Millis(long nanoTime) {
        return nanoTime / 1_000_000;
    }

    public static class ExecutionResult<R> {

        private final R result;
        private final long useTime;

        public ExecutionResult(R result, long useTime) {
            this.result = result;
            this.useTime = useTime;
        }

        public R result() {
            return result;
        }

        public long useTime() {
            return useTime;
        }

    }

}
