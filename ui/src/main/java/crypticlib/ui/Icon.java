package crypticlib.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface Icon {

    /**
     * 执行此图标对应的操作
     * @param event 传入的点击事件
     * @return 图标本身
     */
    Icon click(InventoryClickEvent event);

    ItemStack display();

    Icon setDisplay(ItemStack item);

}
