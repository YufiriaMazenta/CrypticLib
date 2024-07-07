package crypticlib.ui.display;

import crypticlib.util.ItemHelper;
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
        this(new ItemStack(material), null, null, null);
    }

    public Icon(@NotNull Material material, @Nullable Integer customModelData) {
        this(new ItemStack(material), null, null, customModelData);
    }

    public Icon(@NotNull Material material, @Nullable String name) {
        this(new ItemStack(material), name, null, null);
    }

    public Icon(@NotNull Material material, @Nullable List<String> lore) {
        this(new ItemStack(material), null, lore, null);
    }

    public Icon(@NotNull Material material, @Nullable List<String> lore, @Nullable Integer customModelData) {
        this(new ItemStack(material), null, lore, customModelData);
    }

    public Icon(@NotNull Material material, @Nullable String name, @Nullable Integer customModelData) {
        this(new ItemStack(material), name, null, customModelData);
    }

    public Icon(@NotNull Material material, @Nullable String name, @Nullable List<String> lore) {
        this(new ItemStack(material), name, lore, null);
    }

    public Icon(@NotNull Material material, @Nullable String name, @Nullable List<String> lore, @Nullable Integer customModelData) {
        this(new ItemStack(material), name, lore, customModelData);
    }

    public Icon(@NotNull ItemStack display) {
        this(display, null, null, null);
    }

    public Icon(@NotNull ItemStack display, @Nullable String name) {
        this(display, name, null, null);
    }

    public Icon(@NotNull ItemStack display, @Nullable List<String> lore) {
        this(display, null, lore, null);
    }

    public Icon(@NotNull ItemStack display, @Nullable Integer customModelData) {
        this(display, null, null, customModelData);
    }

    public Icon(@NotNull ItemStack display, @Nullable List<String> lore, @Nullable Integer customModelData) {
        this(display, null, lore, customModelData);
    }

    public Icon(@NotNull ItemStack display, @Nullable String name, @Nullable Integer customModelData) {
        this(display, name, null, customModelData);
    }

    public Icon(@NotNull ItemStack display, @Nullable String name, @Nullable List<String> lore) {
        this(display, name, lore, null);
    }

    public Icon(@NotNull ItemStack display, @Nullable String name, @Nullable List<String> lore, @Nullable Integer customModelData) {
        if (!ItemHelper.isAir(display)) {
            ItemStack displayClone = display.clone();
            ItemMeta itemMeta = displayClone.getItemMeta();
            if (itemMeta == null)
                throw new IllegalArgumentException("Item meta can not be null");
            if (name != null)
                itemMeta.setDisplayName(name);
            if (lore != null)
                itemMeta.setLore(lore);
            if (customModelData != null)
                itemMeta.setCustomModelData(customModelData);
            displayClone.setItemMeta(itemMeta);
            this.display = displayClone;
        } else {
            this.display = display;
        }
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
        ItemHelper.setDisplayName(display, name);
        return this;
    }

    public Icon setLore(List<String> lore) {
        ItemHelper.setLore(display, lore);
        return this;
    }

}
