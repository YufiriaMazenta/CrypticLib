package crypticlib.ui.display;

import crypticlib.nms.item.Item;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Consumer;

public class Icon {

    private ItemStack display;
    private Consumer<InventoryClickEvent> clickConsumer;

    public Icon(Material material, String name) {
        this(material, name, event -> event.setCancelled(true));
    }

    public Icon(Material material, String name, Consumer<InventoryClickEvent> clickConsumer) {
        ItemStack display = new ItemStack(material);
        ItemMeta itemMeta = Bukkit.getItemFactory().getItemMeta(material);
        itemMeta.setDisplayName(name);
        display.setItemMeta(itemMeta);
        this.display = display;
        this.clickConsumer = clickConsumer;
    }

    public Icon(ItemStack display) {
        this(display, event -> event.setCancelled(true));
    }

    public Icon(Item item) {
        this(item.buildBukkit());
    }

    public Icon(Item item, Consumer<InventoryClickEvent> clickConsumer) {
        this(item.buildBukkit(), clickConsumer);
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
