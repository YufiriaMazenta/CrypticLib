package crypticlib.ui;

import crypticlib.util.ItemUtil;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public interface ReceptacleMenu extends Menu {

    @NotNull Map<Integer, ItemStack> storedItems();

    @Override
    default Icon click(int slot, InventoryClickEvent event) {
        if (!iconSlotMap().containsKey(slot)) {
            ItemStack current = event.getCurrentItem();
            ItemStack cursor = event.getCursor();
            if (ItemUtil.isAir(current)) {
                if (ItemUtil.isAir(cursor))
                    return null;
            } else {
                if (ItemUtil.isAir(cursor)) {
                    if (storedItems().containsKey(slot)) {
                        return null;
                    }
                    storedItems().put(slot, current);
                    return null;
                }
            }
            storedItems().put(slot, cursor);
            return null;
        }
        return Menu.super.click(slot, event);
    }

    @Override
    default void onClose(InventoryCloseEvent event) {
        ItemStack[] items = storedItems().values().toArray(new ItemStack[]{});
        HashMap<Integer, ItemStack> failedItems = event.getPlayer().getInventory().addItem(items);
        if (failedItems.isEmpty())
            return;
        for (ItemStack item : failedItems.values()) {
            event.getPlayer().getWorld().dropItem(event.getPlayer().getLocation(), item);
        }
    }

}
