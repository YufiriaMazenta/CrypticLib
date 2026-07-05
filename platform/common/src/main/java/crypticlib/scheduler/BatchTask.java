package crypticlib.scheduler;

import crypticlib.CrypticLib;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 通用的分片定时任务，每 tick 处理列表中的 N 个元素。
 * <p>
 * 用法（无返回值）：
 * <pre>{@code
 * BatchTask<File, Void> task = new BatchTask<>(
 *     scheduler,
 *     files,
 *     file -> { loadFile(file); return null; },
 *     12,
 *     (results, batch) -> log("处理了 " + batch.processedCount() + " 个文件")
 * );
 * task.start();
 * }</pre>
 * <p>
 * 用法（有返回值）：
 * <pre>{@code
 * BatchTask<File, Recipe> task = new BatchTask<>(
 *     files,
 *     file -> parseRecipe(file),
 *     12,
 *     (results, batch) -> {
 *         log("解析了 " + results.size() + " 个配方");
 *         registerRecipes(results);
 *     }
 * );
 * task.start();
 * }</pre>
 *
 * @param <T> 输入元素类型
 * @param <R> 输出结果类型（无需返回值时用 Void）
 */
public class BatchTask<T, R> {

    private final List<T> items;
    private final Function<T, R> processor;
    private final int itemsPerTick;
    private final BatchCallback<T, R> callback;
    private final Scheduler scheduler;
    private final List<T> failedItems = new ArrayList<>();
    private final List<R> results = new ArrayList<>();

    private int currentIndex = 0;
    private int useTick = 0;
    private long useMilliseconds = 0;
    private TaskWrapper taskWrapper;

    @FunctionalInterface
    public interface BatchCallback<T, R> {
        void onComplete(List<R> results, BatchTask<T, R> batch);
    }

    /**
     * @param items        待处理的元素列表
     * @param processor    每个元素的处理逻辑，返回结果会被收集；抛出异常时该元素记录到 failedItems
     * @param itemsPerTick 每 tick 最大处理元素数
     * @param callback     全部处理完毕后的回调，接收结果列表和任务实例（可为 null）
     */
    public BatchTask(
        @NotNull List<T> items,
        @NotNull Function<T, R> processor,
        int itemsPerTick,
        @Nullable BatchCallback<T, R> callback
    ) {
        this.scheduler = CrypticLib.scheduler();
        this.items = items;
        this.processor = processor;
        this.itemsPerTick = itemsPerTick;
        this.callback = callback;
    }

    /**
     * 启动分片任务，每 tick 执行一次，每次处理最多 itemsPerTick 个元素。
     */
    public void start() {
        this.taskWrapper = scheduler.syncTimer(this::run, 1L, 1L);
    }

    /**
     * 取消任务。
     */
    public void cancel() {
        if (taskWrapper != null) {
            taskWrapper.cancel();
        }
    }

    private void run() {
        if (currentIndex >= items.size()) {
            finish();
            return;
        }
        long startTime = System.currentTimeMillis();
        int end = Math.min(currentIndex + itemsPerTick, items.size());
        for (int i = currentIndex; i < end; i++) {
            try {
                R result = processor.apply(items.get(i));
                if (result != null) {
                    results.add(result);
                }
            } catch (Exception e) {
                failedItems.add(items.get(i));
            }
        }
        useMilliseconds += System.currentTimeMillis() - startTime;
        useTick++;
        currentIndex = end;
        if (currentIndex >= items.size()) {
            finish();
        }
    }

    private void finish() {
        cancel();
        if (callback != null) {
            callback.onComplete(results, this);
        }
    }

    /** 已处理的元素数量 */
    public int processedCount() {
        return currentIndex;
    }

    /** 处理失败的元素数量 */
    public int failedCount() {
        return failedItems.size();
    }

    /** 已消耗的 tick 数 */
    public int getUseTick() {
        return useTick;
    }

    /** 总耗时（毫秒） */
    public long getUseMilliseconds() {
        return useMilliseconds;
    }

    /** 处理失败的元素列表 */
    public List<T> getFailedItems() {
        return failedItems;
    }

}
