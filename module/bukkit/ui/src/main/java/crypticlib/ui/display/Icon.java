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

    public Icon(@NotNull IconDisplay iconDisplay) {
        this.display = iconDisplay.display();
    }

    public Icon(@NotNull ItemStack display) {
        this(display, null);
    }

    public Icon(@NotNull ItemStack origin, @Nullable IconDisplay iconDisplay) {
        if (!ItemHelper.isAir(origin)) {
            ItemStack originClone = origin.clone();
            if (iconDisplay != null)
                this.display = iconDisplay.applyToItemStack(originClone);
            else
                this.display = originClone;
        } else {
            this.display = origin;
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
