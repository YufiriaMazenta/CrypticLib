package crypticlib.ui.handler;

import crypticlib.listener.BukkitListener;
import crypticlib.ui.Menu;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.InventoryHolder;

@BukkitListener
public class MenuHandler implements Listener {

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
        if (event.getView().getTopInventory().getHolder() instanceof Menu)
            event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseMenu(InventoryCloseEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof Menu))
            return;
        ((Menu) event.getView().getTopInventory().getHolder()).onClose(event);
    }

}
