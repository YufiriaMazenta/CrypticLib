package crypticlib.ui.menu;

import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class StoredMenu extends Menu {

    private final Map<Integer, ItemStack> storedItems;
    private boolean retrieveStoredItems;

    public StoredMenu(Player player, MenuDisplay display) {
        super(player, display);
        storedItems = new ConcurrentHashMap<>();
        retrieveStoredItems = true;
    }

    public StoredMenu(Player player, MenuDisplay display, BiConsumer<Menu, InventoryOpenEvent> openAction, BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        super(player, display, openAction, closeAction);
        storedItems = new ConcurrentHashMap<>();
        retrieveStoredItems = true;
    }

    @Override
    public Icon onClick(int slot, InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) {
            if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || action.equals(InventoryAction.COLLECT_TO_CURSOR))
                event.setCancelled(true);
            return null;
        }
        if (!slotMap().containsKey(slot)) {
            if (action.equals(InventoryAction.COLLECT_TO_CURSOR)) {
                event.setCancelled(true);
                return null;
            }
            refreshStoredItems(event.getClickedInventory());
            return null;
        }
        event.setCancelled(true);
        return slotMap().get(slot).onClick(event);
    }

    public StoredMenu refreshStoredItems(Inventory inventory) {
        storedItems.clear();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slotMap().containsKey(i))
                continue;
            if (ItemUtil.isAir(inventory.getItem(i)))
                continue;
            storedItems.put(i, inventory.getItem(i));
        }
        return this;
    }

    /**
     * 将玩家放入的物品返还
     */
    public void retrieveItems() {
        if (!retrieveStoredItems)
            return;
        ItemStack[] returnItems = new ItemStack[storedItems.size()];
        int i = 0;
        for (Integer slot : storedItems.keySet()) {
            ItemStack item = storedItems.get(slot);
            returnItems[i] = item;
            i ++;
        }
        HashMap<Integer, ItemStack> failedItems = player().getInventory().addItem(returnItems);
        if (failedItems.isEmpty())
            return;
        for (ItemStack item : failedItems.values()) {
            player().getWorld().dropItem(player().getLocation(), item);
        }
        storedItems.clear();
    }

    @Override
    public StoredMenu openMenu() {
        return (StoredMenu) super.openMenu();
    }

    @Override
    public StoredMenu parseLayout() {
        return (StoredMenu) super.parseLayout();
    }

    @Override
    public StoredMenu setDisplay(MenuDisplay display) {
        return (StoredMenu) super.setDisplay(display);
    }

    @Override
    public StoredMenu setOpenAction(BiConsumer<Menu, InventoryOpenEvent> openAction) {
        return (StoredMenu) super.setOpenAction(openAction);
    }

    @Override
    public StoredMenu setCloseAction(BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        return (StoredMenu) super.setCloseAction(closeAction);
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        refreshStoredItems(event.getInventory());
        super.onClose(event);
        retrieveItems();
    }

    public Map<Integer, ItemStack> storedItems() {
        return storedItems;
    }

    public boolean isRetrieveStoredItems() {
        return retrieveStoredItems;
    }

    public StoredMenu setRetrieveStoredItems(boolean retrieveStoredItems) {
        this.retrieveStoredItems = retrieveStoredItems;
        return this;
    }

}
