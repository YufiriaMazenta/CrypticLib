package crypticlib.ui.handler;

import crypticlib.listener.EventListener;
import crypticlib.ui.menu.Menu;
import crypticlib.ui.menu.StoredMenu;
import crypticlib.util.ReflectionHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.lang.reflect.Method;

@EventListener
public enum MenuHandler implements Listener {

    INSTANCE;
    private final Method inventoryEventGetViewMethod;
    private final Method inventoryViewGetTopInventoryMethod;
    private final Method playerGetOpenInventoryMethod;

    MenuHandler() {
        try {
            inventoryEventGetViewMethod = ReflectionHelper.getMethod(InventoryEvent.class, "getView");
            Class<?> inventoryViewClass = Class.forName("org.bukkit.inventory.InventoryView");
            inventoryViewGetTopInventoryMethod = ReflectionHelper.getMethod(inventoryViewClass, "getTopInventory");
            playerGetOpenInventoryMethod = ReflectionHelper.getMethod(Player.class, "getOpenInventory");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickMenu(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;
        InventoryHolder holder = getTopInventory(getInventoryView(event)).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onClick(event.getSlot(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragMenu(InventoryDragEvent event) {
        InventoryHolder holder = getTopInventory(getInventoryView(event)).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onDrag(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenMenu(InventoryOpenEvent event) {
        InventoryHolder holder = getTopInventory(getInventoryView(event)).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onOpen(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseMenu(InventoryCloseEvent event) {
        InventoryHolder holder = getTopInventory(getInventoryView(event)).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onClose(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryHolder topInvHolder = getTopInventory(getOpenInventory(player)).getHolder();
        if (topInvHolder instanceof StoredMenu) {
            ((StoredMenu) topInvHolder).refreshStoredItems(event.getPlayer().getInventory()).returnStoredItems();
        }
    }

    private Inventory getTopInventory(Object inventoryView) {
        return (Inventory) ReflectionHelper.invokeMethod(inventoryViewGetTopInventoryMethod, inventoryView);
    }

    private Object getInventoryView(InventoryEvent event) {
        return ReflectionHelper.invokeMethod(inventoryEventGetViewMethod, event);
    }

    private Object getOpenInventory(Player player) {
        return ReflectionHelper.invokeMethod(playerGetOpenInventoryMethod, player);
    }

}
