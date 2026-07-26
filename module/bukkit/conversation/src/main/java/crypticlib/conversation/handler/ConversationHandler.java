package crypticlib.conversation.handler;

import crypticlib.conversation.Conversation;
import crypticlib.listener.EventListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventListener
public enum ConversationHandler implements Listener {

    INSTANCE;
    private final Map<UUID, Conversation> conversationMap;

    ConversationHandler() {
        conversationMap = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Conversation conversation = conversationMap.get(uuid);
        if (conversation == null)
            return;
        event.setCancelled(true);
        conversation.handleInput(event.getMessage());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        endChat(event.getPlayer());
    }

    public void startChat(Player player, Conversation conversation) {
        Conversation previous = conversationMap.put(player.getUniqueId(), conversation);
        if (previous != null && previous != conversation) {
            previous.abandon();
        }
    }

    public void endChat(Player player) {
        Conversation conversation = conversationMap.remove(player.getUniqueId());
        if (conversation != null) {
            conversation.abandon();
        }
    }

    /**
     * 仅当映射中当前会话仍是指定会话时才移除, 并取消其超时任务。
     * 避免旧会话结束时误删玩家的新会话。
     */
    public void endChat(Player player, Conversation conversation) {
        if (conversationMap.remove(player.getUniqueId(), conversation)) {
            conversation.abandon();
        }
    }

    /**
     * 判断指定会话是否仍是该玩家当前注册的会话。
     */
    public boolean isCurrent(Player player, Conversation conversation) {
        return conversationMap.get(player.getUniqueId()) == conversation;
    }

}
