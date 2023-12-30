package crypticlib.ui.handler;

import crypticlib.listener.BukkitListener;
import crypticlib.ui.menu.Menu;
import crypticlib.ui.menu.StoredMenu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

@BukkitListener
public enum MenuHandler implements Listener {

    INSTANCE;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickMenu(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onClick(event.getSlot(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragMenu(InventoryDragEvent event) {
        InventoryHolder holder = event.getView().getTopInventory().getHolder();
        if (!(holder instanceof Menu)) {
            return;
        }
        if (event.getWhoClicked().getInventory().equals(event.getInventory()))
            return;
        for (Integer slot : event.getInventorySlots()) {
            ItemStack current = event.getInventory().getItem(slot);
            if (event.getOldCursor().isSimilar(current)) {
                event.setCancelled(true);
                return;
            }
        }
        if (holder instanceof StoredMenu) {
            ((StoredMenu) holder).refreshStoredItems(event.getInventory());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenMenu(InventoryOpenEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu))
            return;
        ((Menu) event.getView().getTopInventory().getHolder()).onOpen(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseMenu(InventoryCloseEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu))
            return;
        ((Menu) event.getView().getTopInventory().getHolder()).onClose(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        InventoryHolder inventoryHolder = event.getPlayer().getInventory().getHolder();
        if (inventoryHolder instanceof StoredMenu) {
            ((StoredMenu) inventoryHolder).refreshStoredItems(event.getPlayer().getInventory()).returnItems();
        }
    }

}
