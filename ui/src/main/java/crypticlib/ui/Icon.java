package crypticlib.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public class Icon {

    private ItemStack display;
    private Consumer<InventoryClickEvent> clickConsumer;

    public Icon(ItemStack display) {
        this(display, event -> event.setCancelled(true));
    }

    public Icon(ItemStack display, Consumer<InventoryClickEvent> clickConsumer) {
        this.display = display;
        this.clickConsumer = clickConsumer;
    }

    /**
     * 执行此图标对应的操作
     * @param event 传入的点击事件
     * @return 图标本身
     */
    public Icon onClick(InventoryClickEvent event) {
        if (clickConsumer == null) {
            event.setCancelled(true);
            return this;
        }
        clickConsumer.accept(event);
        return this;
    }

    /**
     * 图标的展示物品
     * @return 图标的展示物品
     */
    public ItemStack display() {
        return display;
    }

    /**
     * 设置图标的展示物品
     * @param display 设置的展示物品
     */
    public Icon setDisplay(ItemStack display) {
        this.display = display;
        return this;
    }

    public Consumer<InventoryClickEvent> clickConsumer() {
        return clickConsumer;
    }

    public Icon setClickConsumer(Consumer<InventoryClickEvent> clickConsumer) {
        this.clickConsumer = clickConsumer;
        return this;
    }

}
