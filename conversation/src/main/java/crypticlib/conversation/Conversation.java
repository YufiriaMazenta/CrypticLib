package crypticlib.conversation;

import crypticlib.CrypticLib;
import crypticlib.chat.MessageSender;
import crypticlib.conversation.handler.ConversationHandler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Conversation {

    private final Player who;
    private final Plugin plugin;
    private Prompt prompt;
    private String cancelInput;
    private final Map<Object, Object> data;
    private Consumer<Map<Object, Object>> endTask;

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, @NotNull Prompt firstPrompt) {
        this.plugin = plugin;
        this.who = who;
        this.prompt = firstPrompt;
        this.data = new ConcurrentHashMap<>();
        this.cancelInput = "cancel";
        this.endTask = null;
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, @NotNull Prompt firstPrompt, Consumer<Map<Object, Object>> endTask) {
        this.plugin = plugin;
        this.who = who;
        this.prompt = firstPrompt;
        this.data = new ConcurrentHashMap<>();
        this.cancelInput = "cancel";
        this.endTask = endTask;
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, @NotNull Prompt firstPrompt, @NotNull String cancelInput) {
        this.plugin = plugin;
        this.who = who;
        this.prompt = firstPrompt;
        this.data = new ConcurrentHashMap<>();
        this.cancelInput = cancelInput;
        this.endTask = null;
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, @NotNull Prompt firstPrompt, @NotNull String cancelInput, Consumer<Map<Object, Object>> endTask) {
        this.plugin = plugin;
        this.who = who;
        this.prompt = firstPrompt;
        this.data = new ConcurrentHashMap<>();
        this.cancelInput = cancelInput;
        this.endTask = endTask;
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, Prompt firstPrompt, Map<Object, Object> data, String cancelInput) {
        this.plugin = plugin;
        this.who = who;
        this.prompt = firstPrompt;
        this.data = data;
        this.cancelInput = cancelInput;
        this.endTask = null;
    }

    public Conversation(@NotNull Plugin plugin, @NotNull Player who, Prompt firstPrompt, Map<Object, Object> data, String cancelInput, Consumer<Map<Object, Object>> endTask) {
        this.plugin = plugin;
        this.who = who;
        this.prompt = firstPrompt;
        this.data = data;
        this.cancelInput = cancelInput;
        this.endTask = endTask;
    }

    public void start() {
        ConversationHandler.INSTANCE.startChat(who, this);
        MessageSender.sendMsg(who, prompt.promptText(data));
    }

    public void end() {
        ConversationHandler.INSTANCE.endChat(who);
        if (endTask != null)
            endTask.accept(data);
    }

    public void handleInput(String input) {
        CrypticLib.platform().scheduler().runTask(plugin, () -> {
            if (input.equalsIgnoreCase(cancelInput)) {
                end();
                return;
            }
            prompt = prompt.acceptInput(data, input);
            if (prompt == null) {
                end();
                return;
            }
            MessageSender.sendMsg(who, prompt.promptText(data));
        });
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
