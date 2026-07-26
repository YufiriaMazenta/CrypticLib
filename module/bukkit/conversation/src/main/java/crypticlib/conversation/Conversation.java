package crypticlib.conversation;

import crypticlib.CrypticLibBukkit;
import crypticlib.chat.BukkitMsgSender;
import crypticlib.BukkitPlayer;
import crypticlib.conversation.handler.ConversationHandler;
import crypticlib.scheduler.TaskWrapper;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class Conversation {

    /**
     * 默认超时时间: 30秒 = 600 tick
     */
    public static final long DEFAULT_TIMEOUT_TICKS = 600L;

    private final Player who;
    private final Plugin plugin;
    private Prompt prompt;
    private String cancelInput;
    private final Map<Object, Object> data;
    private Consumer<Map<Object, Object>> endTask;

    private long timeoutTicks;
    private volatile @Nullable TaskWrapper timeoutTaskWrapper;
    private @Nullable Consumer<Conversation> onTimeout;
    private final AtomicBoolean ended = new AtomicBoolean(false);

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, @NotNull Prompt firstPrompt) {
        this(plugin, who, firstPrompt, "cancel", null);
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, @NotNull Prompt firstPrompt, Consumer<Map<Object, Object>> endTask) {
        this(plugin, who, firstPrompt, "cancel", endTask);
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, @NotNull Prompt firstPrompt, @NotNull String cancelInput) {
        this(plugin, who, firstPrompt, cancelInput, null);
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, @NotNull Prompt firstPrompt, @NotNull String cancelInput, Consumer<Map<Object, Object>> endTask) {
        this.plugin = plugin;
        this.who = who;
        this.prompt = firstPrompt;
        this.data = new ConcurrentHashMap<>();
        this.cancelInput = cancelInput;
        this.endTask = endTask;
        this.timeoutTicks = DEFAULT_TIMEOUT_TICKS;
        this.timeoutTaskWrapper = null;
        this.onTimeout = null;
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, Prompt firstPrompt, Map<Object, Object> data, String cancelInput) {
        this(plugin, who, firstPrompt, data, cancelInput, null);
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, Prompt firstPrompt, Map<Object, Object> data, String cancelInput, Consumer<Map<Object, Object>> endTask) {
        this.plugin = plugin;
        this.who = who;
        this.prompt = firstPrompt;
        this.data = data;
        this.cancelInput = cancelInput;
        this.endTask = endTask;
        this.timeoutTicks = DEFAULT_TIMEOUT_TICKS;
        this.timeoutTaskWrapper = null;
        this.onTimeout = null;
    }

    public void start() {
        ConversationHandler.INSTANCE.startChat(who, this);
        BukkitMsgSender.INSTANCE.sendMsg(BukkitPlayer.byPlayer(who), prompt.promptText(data));
        scheduleTimeout();
    }

    public void end() {
        if (!ended.compareAndSet(false, true)) {
            return;
        }
        cancelTimeout();
        ConversationHandler.INSTANCE.endChat(who, this);
        if (endTask != null)
            endTask.accept(data);
    }

    /**
     * 放弃当前会话: 取消挂起的超时任务并标记会话已结束, 但不触发 endTask 回调。
     * 用于玩家退出、被新会话覆盖等外部清理场景, 与 {@link #end()} 区分开。
     */
    public void abandon() {
        ended.set(true);
        cancelTimeout();
    }

    public void handleInput(String input) {
        CrypticLibBukkit.scheduler().runOnEntity(who, () -> {
            if (ended.get() || !ConversationHandler.INSTANCE.isCurrent(who, this)) {
                return;
            }
            cancelTimeout();
            if (input.equalsIgnoreCase(cancelInput)) {
                end();
                return;
            }
            prompt = prompt.acceptInput(data, input);
            if (prompt == null) {
                end();
                return;
            }
            BukkitMsgSender.INSTANCE.sendMsg(BukkitPlayer.byPlayer(who), prompt.promptText(data));
            scheduleTimeout();
        }, () -> ConversationHandler.INSTANCE.endChat(who, this));
    }


    /**
     * 启动线程在指定的时间后判定为超时
     */
    private void scheduleTimeout() {
        if (timeoutTicks <= 0) {
            return;
        }
        timeoutTaskWrapper = CrypticLibBukkit.scheduler().runOnEntityLater(who, () -> {
            if (ended.get() || !ConversationHandler.INSTANCE.isCurrent(who, this)) {
                return;
            }
            if (onTimeout != null) {
                onTimeout.accept(this);
            }
            end();
        }, () -> ConversationHandler.INSTANCE.endChat(who, this), timeoutTicks);
    }

    /**
     * 取消掉当前的超时判定任务
     */
    private void cancelTimeout() {
        if (timeoutTaskWrapper != null && !timeoutTaskWrapper.isCancelled()) {
            timeoutTaskWrapper.cancel();
            timeoutTaskWrapper = null;
        }
    }

    /**
     * 获取超时时间(tick), 1秒=20tick
     * @return 超时时间tick数, 0或负数表示不超时
     */
    public long timeoutTicks() {
        return timeoutTicks;
    }

    /**
     * 设置超时时间(tick), 1秒=20tick
     * @param timeoutTicks 超时时间tick数, 0或负数表示不超时
     * @return this
     */
    public Conversation setTimeoutTicks(long timeoutTicks) {
        this.timeoutTicks = timeoutTicks;
        return this;
    }

    /**
     * 设置超时时的回调, 默认为不进行任何操作
     * @param onTimeout 超时回调, null则不进行任何操作
     * @return this
     */
    public Conversation setOnTimeout(@Nullable Consumer<Conversation> onTimeout) {
        this.onTimeout = onTimeout;
        return this;
    }

    public Consumer<Map<Object, Object>> endTask() {
        return endTask;
    }

    public Conversation setEndTask(Consumer<Map<Object, Object>> endTask) {
        this.endTask = endTask;
        return this;
    }

    public String cancelInput() {
        return cancelInput;
    }

    public Conversation setCancelInput(String cancelInput) {
        this.cancelInput = cancelInput;
        return this;
    }

    public Plugin plugin() {
        return plugin;
    }

    public Map<Object, Object> data() {
        return data;
    }

    public Player who() {
        return who;
    }

}
