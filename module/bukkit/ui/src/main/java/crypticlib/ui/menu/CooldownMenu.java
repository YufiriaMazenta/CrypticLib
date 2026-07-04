package crypticlib.ui.menu;

import crypticlib.BukkitPlayer;
import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.util.InventoryViewHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

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
    public Optional<Icon> onClick(int slot, InventoryClickEvent event) {
        if (!InventoryViewHelper.getTopInventory(event).equals(event.getClickedInventory())) {
            event.setCancelled(true);
            return Optional.empty();
        }
        if (!slotMap.containsKey(slot)) {
            event.setCancelled(true);
            return Optional.empty();
        }
        event.setCancelled(true);
        long current = System.currentTimeMillis();
        long cooldown = cooldownTick * 50L - (current - lastClick);
        if (cooldown > 0) {
            Optional<Player> playerOpt = playerOpt();
            if (playerOpt.isPresent()) {
                new BukkitPlayer(playerOpt.orElse(null)).sendMsg(
                    String.format(
                        cooldownMessage, cooldown / 1000.0
                    )
                );
            }
            return Optional.empty();
        }
        this.lastClick = current;
        return Optional.of(slotMap.get(slot).onClick(event));
    }

    @Override
    public CooldownMenu setDisplay(@NotNull MenuDisplay display) {
        return (CooldownMenu) super.setDisplay(display);
    }

}
