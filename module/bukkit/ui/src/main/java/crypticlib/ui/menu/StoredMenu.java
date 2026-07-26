package crypticlib.ui.menu;

import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.util.InventoryViewHelper;
import crypticlib.util.ItemHelper;
import org.bukkit.Material;
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
import java.util.Objects;
import java.util.Optional;
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
    public Optional<Icon> onClick(int slot, InventoryClickEvent event) {
        InventoryAction action = event.getAction();
        Inventory topInv = InventoryViewHelper.getTopInventory(event);
        if (!topInv.equals(event.getClickedInventory())) {
            if (action.equals(InventoryAction.MOVE_TO_OTHER_INVENTORY) || action.equals(InventoryAction.COLLECT_TO_CURSOR))
                event.setCancelled(true);
            return Optional.empty();
        }
        if (!slotMap.containsKey(slot)) {
            if (action.equals(InventoryAction.COLLECT_TO_CURSOR)) {
                event.setCancelled(true);
            }
            return Optional.empty();
        }
        event.setCancelled(true);
        refreshStoredItems(event.getClickedInventory());
        return Optional.of(slotMap.get(slot).onClick(event));
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
        Player player = player().orElse(null);
        ItemStack[] returnItems = new ItemStack[storedItems.size()];
        int i = 0;
        for (Integer slot : storedItems.keySet()) {
            ItemStack item = storedItems.get(slot);
            //归还前clone,避免归还的物品仍是菜单容器槽位的镜像被后续操作反向修改
            returnItems[i] = item != null ? item.clone() : null;
            i++;
            //将已归还的物品从菜单容器对应槽位移除,防止菜单复用时物品残留被再次取出造成复制
            if (inventoryCache != null)
                inventoryCache.setItem(slot, new ItemStack(Material.AIR));
        }
        //无条件清空storedItems,避免下次开关循环重复归还
        storedItems.clear();
        if (player == null) {
            return;
        }
        HashMap<Integer, ItemStack> failedItems = player.getInventory().addItem(returnItems);
        if (failedItems.isEmpty())
            return;
        for (ItemStack item : failedItems.values()) {
            player.getWorld().dropItem(player.getLocation(), item);
        }
    }

    @Override
    public StoredMenu setDisplay(@NotNull MenuDisplay display) {
        return (StoredMenu) super.setDisplay(display);
    }

    @Override
    public void onDrag(InventoryDragEvent event) {
        //判断拖拽操作是否在顶部UI进行
        Object inventoryView = InventoryViewHelper.getInventoryView(event);
        boolean rawSlotsInTopInv = false;
        for (Integer rawSlot : event.getRawSlots()) {
            Inventory inventory = InventoryViewHelper.getInventory(inventoryView, rawSlot);
            if (Objects.equals(inventory, InventoryViewHelper.getTopInventory(event))) {
                rawSlotsInTopInv = true;
            }
        }

        //如果包含顶部UI,将进行判断
        if (rawSlotsInTopInv) {
            for (Integer slot : event.getInventorySlots()) {
                //不允许对已有图标槽位的拖拽
                if (slotMap.containsKey(slot)) {
                    event.setCancelled(true);
                    return;
                }
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
