package crypticlib.ui;

import crypticlib.util.ItemUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class StoredMenu extends Menu {

    private final Map<Integer, ItemStack> storedItems;
    private final List<InventoryAction> disallowActions;
    private boolean returnStoredItems;

    public StoredMenu(Player player, MenuDisplay display) {
        super(player, display);
        storedItems = new ConcurrentHashMap<>();
        disallowActions = new ArrayList<>();
        disallowActions.add(InventoryAction.COLLECT_TO_CURSOR);
        returnStoredItems = true;
    }

    public StoredMenu(Player player, MenuDisplay display, BiConsumer<Menu, InventoryOpenEvent> openAction, BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        super(player, display, openAction, closeAction);
        storedItems = new ConcurrentHashMap<>();
        disallowActions = new ArrayList<>();
        disallowActions.add(InventoryAction.COLLECT_TO_CURSOR);
        returnStoredItems = true;
    }

    @Override
    public Icon onClick(int slot, InventoryClickEvent event) {
        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) {
            if (disallowActions.contains(event.getAction()))
                event.setCancelled(true);
            return null;
        }
        if (!slotMap().containsKey(slot)) {
            if (disallowActions.contains(event.getAction())) {
                event.setCancelled(true);
                return null;
            }
            refreshStoredItems(event.getClickedInventory());
            return null;
        }
        event.setCancelled(true);
        return slotMap().get(slot).onClick(event);
    }

    protected StoredMenu refreshStoredItems(Inventory inventory) {
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

    protected StoredMenu returnStoredItems() {
        ItemStack[] returnItems = new ItemStack[storedItems.size()];
        int i = 0;
        for (Integer slot : storedItems.keySet()) {
            ItemStack item = storedItems.get(slot);
            returnItems[i] = item;
            i ++;
        }
        HashMap<Integer, ItemStack> failedItems = player().getInventory().addItem(returnItems);
        if (failedItems.isEmpty())
            return this;
        for (ItemStack item : failedItems.values()) {
            player().getWorld().dropItem(player().getLocation(), item);
        }
        storedItems.clear();
        return this;
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
        if (returnStoredItems)
            returnStoredItems();
    }

    public Map<Integer, ItemStack> storedItems() {
        return storedItems;
    }

    public boolean isReturnStoredItems() {
        return returnStoredItems;
    }

    public StoredMenu setReturnStoredItems(boolean returnStoredItems) {
        this.returnStoredItems = returnStoredItems;
        return this;
    }

}
