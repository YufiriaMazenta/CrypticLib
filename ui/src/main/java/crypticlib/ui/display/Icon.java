package crypticlib.ui.display;

import crypticlib.nms.item.NbtItem;
import crypticlib.util.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class Icon {

    private ItemStack display;
    private Consumer<InventoryClickEvent> clickAction;

    public Icon(@NotNull Material material) {
        this(material, null);
    }

    public Icon(@NotNull Material material, @Nullable String name) {
        this(material, name, event -> event.setCancelled(true));
    }

    public Icon(@NotNull Material material, @Nullable String name, @Nullable List<String> lore) {
        this(material, name, lore, event -> event.setCancelled(true));
    }

    public Icon(@NotNull Material material, @Nullable String name, @Nullable Consumer<InventoryClickEvent> clickAction) {
        this(material, name, null, clickAction);
    }

    public Icon(@NotNull Material material, @Nullable String name, @Nullable List<String> lore, @Nullable Consumer<InventoryClickEvent> clickAction) {
        this(new ItemStack(material), name, lore, clickAction);
    }

    public Icon(@NotNull ItemStack display) {
        this(display, event -> event.setCancelled(true));
    }

    public Icon(@NotNull ItemStack display, Consumer<InventoryClickEvent> clickAction) {
        this(display, null, null, clickAction);
    }

    public Icon(@NotNull ItemStack display, @Nullable String name) {
        this(display, name, null, event -> event.setCancelled(true));
    }

    public Icon(@NotNull ItemStack display, @Nullable String name, @Nullable Consumer<InventoryClickEvent> clickAction) {
        this(display, name, null, clickAction);
    }

    public Icon(@NotNull ItemStack display, @Nullable String name, @Nullable List<String> lore) {
        this(display, name, lore, event -> event.setCancelled(true));
    }

    public Icon(@NotNull ItemStack display, @Nullable String name, @Nullable List<String> lore, @Nullable Consumer<InventoryClickEvent> clickAction) {
        if (!ItemUtil.isAir(display)) {
            ItemStack displayClone = display.clone();
            ItemMeta itemMeta = displayClone.getItemMeta();
            if (name != null)
                itemMeta.setDisplayName(name);
            if (lore != null)
                itemMeta.setLore(lore);
            displayClone.setItemMeta(itemMeta);
            this.display = displayClone;
        } else {
            this.display = display;
        }
        this.clickAction = clickAction;
    }

    public Icon(@NotNull NbtItem item) {
        this(item.saveNbtToItem());
    }

    public Icon(@NotNull NbtItem item, Consumer<InventoryClickEvent> clickAction) {
        this(item.saveNbtToItem(), clickAction);
    }

    public Icon(@NotNull NbtItem item, @Nullable String name) {
        this(item.saveNbtToItem(), name);
    }

    public Icon(@NotNull NbtItem item, @Nullable String name, @Nullable Consumer<InventoryClickEvent> clickAction) {
        this(item.saveNbtToItem(), name, clickAction);
    }

    public Icon(@NotNull NbtItem item, @Nullable String name, @Nullable List<String> lore) {
        this(item.saveNbtToItem(), name, lore);
    }

    public Icon(@NotNull NbtItem item, @Nullable String name, @Nullable List<String> lore, @Nullable Consumer<InventoryClickEvent> clickAction) {
        this(item.saveNbtToItem(), name, lore, clickAction);
    }

    /**
     * 执行此图标对应的操作
     *
     * @param event 传入的点击事件
     * @return 图标本身
     */
    public Icon onClick(InventoryClickEvent event) {
        if (clickAction == null) {
            event.setCancelled(true);
            return this;
        }
        clickAction.accept(event);
        return this;
    }

    /**
     * 图标的展示物品
     *
     * @return 图标的展示物品
     */
    public ItemStack display() {
        return display;
    }

    /**
     * 设置图标的展示物品
     *
     * @param display 设置的展示物品
     */
    public Icon setDisplay(@NotNull ItemStack display) {
        this.display = display;
        return this;
    }

    @Nullable
    public Consumer<InventoryClickEvent> clickAction() {
        return clickAction;
    }

    public Icon setClickAction(@Nullable Consumer<InventoryClickEvent> clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public Icon setName(String name) {
        ItemUtil.setDisplayName(display, name);
        return this;
    }

    public Icon setLore(List<String> lore) {
        ItemUtil.setLore(display, lore);
        return this;
    }

}
