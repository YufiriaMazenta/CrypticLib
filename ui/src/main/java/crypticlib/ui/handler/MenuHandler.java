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
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu))
            return;
        ((Menu) event.getView().getTopInventory().getHolder()).onDrag(event);
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
            ((StoredMenu) inventoryHolder).refreshStoredItems(event.getPlayer().getInventory()).returnStoredItems();
        }
    }

}
