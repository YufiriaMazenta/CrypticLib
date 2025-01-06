package crypticlib.ui.menu;

import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.util.InventoryViewHelper;
import crypticlib.util.ItemHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class StoredMenu extends Menu {

    protected final Map<Integer, ItemStack> storedItems = new ConcurrentHashMap<>();
    protected Boolean returnStoredItems = true;

    public StoredMenu(@NotNull Player player) {
        super(player);
    }

    public StoredMenu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier) {
        super(player, displaySupplier);
    }

    public StoredMenu(@NotNull Player player, @NotNull MenuDisplay display) {
        super(player, display);
    }

    @Override
    public Icon onClick(int slot, InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        Inventory topInv = InventoryViewHelper.getTopInventory(event);
        if (!topInv.equals(event.getClickedInventory())) {
            if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || action.equals(InventoryAction.COLLECT_TO_CURSOR))
                event.setCancelled(true);
            return null;
        }
        if (!slotMap.containsKey(slot)) {
            if (action.equals(InventoryAction.COLLECT_TO_CURSOR)) {
                event.setCancelled(true);
            }
            return null;
        }
        event.setCancelled(true);
        refreshStoredItems(event.getClickedInventory());
        return slotMap.get(slot).onClick(event);
    }

    public StoredMenu refreshStoredItems(@NotNull Inventory inventory) {
        storedItems.clear();
        for (int i = 0; i < inventory.getSize(); i++) {
            if (slotMap.containsKey(i))
                continue;
            if (ItemHelper.isAir(inventory.getItem(i)))
                continue;
            storedItems.put(i, inventory.getItem(i));
        }
        return this;
    }

    /**
     * 将玩家放入的物品返还
     */
    public void returnStoredItems() {
        if (!returnStoredItems)
            return;
        ItemStack[] returnItems = new ItemStack[storedItems.size()];
        int i = 0;
        for (Integer slot : storedItems.keySet()) {
            ItemStack item = storedItems.get(slot);
            returnItems[i] = item;
            i++;
        }
        HashMap<Integer, ItemStack> failedItems = player.getInventory().addItem(returnItems);
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
    public StoredMenu setDisplay(@NotNull MenuDisplay display) {
        return (StoredMenu) super.setDisplay(display);
    }

    @Override
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked().getInventory().equals(event.getInventory()))
            return;
        for (Integer slot : event.getInventorySlots()) {
            ItemStack current = event.getInventory().getItem(slot);
            if (event.getOldCursor().isSimilar(current)) {
                event.setCancelled(true);
                return;
            }
        }
        refreshStoredItems(event.getInventory());
    }

    @Override
    public void onClose(InventoryCloseEvent event) {
        refreshStoredItems(event.getInventory());
        super.onClose(event);
        returnStoredItems();
    }

    @NotNull
    public Map<Integer, ItemStack> storedItems() {
        return storedItems;
    }

    public Boolean isReturnStoredItems() {
        return returnStoredItems;
    }

    public StoredMenu setReturnStoredItems(Boolean returnStoredItems) {
        this.returnStoredItems = returnStoredItems;
        return this;
    }

}
