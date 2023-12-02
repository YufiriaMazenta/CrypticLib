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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class StoredMenu extends Menu {

    private final Map<Integer, ItemStack> storedItems;
    private boolean returnStoredItems;

    public StoredMenu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier) {
        super(player, displaySupplier);
        this.storedItems = new ConcurrentHashMap<>();
        this.returnStoredItems = true;
    }

    public StoredMenu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier, @Nullable BiConsumer<Menu, InventoryOpenEvent> openAction, @Nullable BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        super(player, displaySupplier, openAction, closeAction);
        this.storedItems = new ConcurrentHashMap<>();
        this.returnStoredItems = true;
    }

    public StoredMenu(@NotNull Player player, @NotNull MenuDisplay display) {
        super(player, display);
        storedItems = new ConcurrentHashMap<>();
        returnStoredItems = true;
    }

    public StoredMenu(@NotNull Player player, @NotNull MenuDisplay display, @Nullable BiConsumer<Menu, InventoryOpenEvent> openAction, @Nullable BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        super(player, display, openAction, closeAction);
        storedItems = new ConcurrentHashMap<>();
        returnStoredItems = true;
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
            }
            return null;
        }
        event.setCancelled(true);
        refreshStoredItems(event.getClickedInventory());
        return slotMap().get(slot).onClick(event);
    }

    public StoredMenu refreshStoredItems(@NotNull Inventory inventory) {
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
    public void returnItems() {
        if (!returnStoredItems)
            return;
        ItemStack[] returnItems = new ItemStack[storedItems.size()];
        int i = 0;
        for (Integer slot : storedItems.keySet()) {
            ItemStack item = storedItems.get(slot);
            returnItems[i] = item;
            i++;
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
    public StoredMenu parseDisplay() {
        return (StoredMenu) super.parseDisplay();
    }

    @Override
    public StoredMenu setDisplay(@NotNull MenuDisplay display) {
        return (StoredMenu) super.setDisplay(display);
    }

    @Override
    public StoredMenu setOpenAction(@Nullable BiConsumer<Menu, InventoryOpenEvent> openAction) {
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
        returnItems();
    }

    @NotNull
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
