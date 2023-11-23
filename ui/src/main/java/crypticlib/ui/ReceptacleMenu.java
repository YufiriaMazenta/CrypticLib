package crypticlib.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.Map;

public interface ReceptacleMenu extends Menu {

    Map<Integer, ItemStack> storedItems();

    @Override
    default Icon click(int slot, InventoryClickEvent event) {
        if (!iconMap().containsKey(slot)) {
            //TODO 存储物品
        }
        return Menu.super.click(slot, event);
    }

    @Override
    default void onClose(InventoryCloseEvent event) {

    }

}
