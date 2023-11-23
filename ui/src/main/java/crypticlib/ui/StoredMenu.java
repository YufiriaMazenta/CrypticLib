package crypticlib.ui;

import crypticlib.util.ItemUtil;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class StoredMenu extends Menu {

    private final List<ItemStack> storedItems;
    private final List<InventoryAction> disallowActions;

    public StoredMenu(List<String> layout, Map<Character, Icon> layoutIconMap, String title) {
        super(layout, layoutIconMap, title);
        storedItems = new ArrayList<>();
        disallowActions = new ArrayList<>();
        disallowActions.add(InventoryAction.COLLECT_TO_CURSOR);
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

    public StoredMenu refreshStoredItems(Inventory inventory) {
        storedItems.clear();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slotMap().containsKey(i))
                continue;
            if (ItemUtil.isAir(inventory.getItem(i)))
                continue;
            storedItems.add(inventory.getItem(i));
        }
        return this;
    }

    @Override
    public StoredMenu onClose(InventoryCloseEvent event) {
        refreshStoredItems(event.getInventory());
        ItemStack[] returnItems = new ItemStack[storedItems.size()];
        for (int i = 0; i < storedItems.size(); i++) {
            returnItems[i] = storedItems.get(i);
        }
        HashMap<Integer, ItemStack> failedItems = event.getPlayer().getInventory().addItem(returnItems);
        if (failedItems.isEmpty())
            return this;
        for (ItemStack item : failedItems.values()) {
            event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), item);
        }
        storedItems.clear();
        return this;
    }

    public List<ItemStack> storedItems() {
        return storedItems;
    }

}
