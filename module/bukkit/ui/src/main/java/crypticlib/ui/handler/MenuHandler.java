package crypticlib.ui.handler;

import crypticlib.listener.EventListener;
import crypticlib.ui.menu.Menu;
import crypticlib.ui.menu.StoredMenu;
import crypticlib.util.InventoryViewHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.InventoryHolder;

@EventListener
public enum MenuHandler implements Listener {

    INSTANCE;


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickMenu(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;
        InventoryHolder holder = InventoryViewHelper.getTopInventory(event).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onClick(event.getSlot(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragMenu(InventoryDragEvent event) {
        InventoryHolder holder = InventoryViewHelper.getTopInventory(event).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onDrag(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenMenu(InventoryOpenEvent event) {
        InventoryHolder holder = InventoryViewHelper.getTopInventory(event).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onOpen(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseMenu(InventoryCloseEvent event) {
        InventoryHolder holder = InventoryViewHelper.getTopInventory(event).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onClose(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryHolder topInvHolder = InventoryViewHelper.getTopInventory(player).getHolder();
        if (topInvHolder instanceof StoredMenu) {
            ((StoredMenu) topInvHolder).refreshStoredItems(event.getPlayer().getInventory()).returnStoredItems();
        }
    }

}