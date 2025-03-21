package crypticlib.ui.menu;

import crypticlib.chat.BukkitMsgSender;
import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.util.InventoryViewHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class CooldownMenu extends Menu {

    private final int cooldownTick;
    private final String cooldownMessage;
    private long lastClick = 0;

    public CooldownMenu(@NotNull Player player, @NotNull MenuDisplay display, int cooldownTick, String cooldownMessage) {
        super(player, display);
        this.cooldownTick = cooldownTick;
        this.cooldownMessage = cooldownMessage;
    }

    @Override
    public Icon onClick(int slot, InventoryClickEvent event) {
        if (!InventoryViewHelper.getTopInventory(event).equals(event.getClickedInventory())) {
            event.setCancelled(true);
            return null;
        }
        if (!slotMap.containsKey(slot)) {
            event.setCancelled(true);
            return null;
        }
        event.setCancelled(true);
        long current = System.currentTimeMillis();
        long cooldown = cooldownTick * 50L - (current - lastClick);
        if (cooldown > 0) {
            BukkitMsgSender.INSTANCE.sendMsg(
                player(),
                String.format(
                    cooldownMessage, cooldown / 1000.0
                )
            );
            return null;
        }
        this.lastClick = current;
        return slotMap.get(slot).onClick(event);
    }

}
