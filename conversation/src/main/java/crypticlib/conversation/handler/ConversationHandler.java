package crypticlib.conversation.handler;

import crypticlib.CrypticLib;
import crypticlib.conversation.Conversation;
import crypticlib.listener.BukkitListener;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@BukkitListener
public enum ConversationHandler implements Listener {

    INSTANCE;
    private final Map<UUID, Conversation> conversationMap;

    ConversationHandler() {
        conversationMap = new ConcurrentHashMap<>();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!conversationMap.containsKey(uuid))
            return;
        event.setCancelled(true);
        conversationMap.get(uuid).handleInput(event.getMessage());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        endChat(event.getPlayer());
    }

    public void startChat(Player player, Conversation conversation) {
        conversationMap.put(player.getUniqueId(), conversation);
    }

    public void endChat(Player player) {
        conversationMap.remove(player.getUniqueId());
    }

}
