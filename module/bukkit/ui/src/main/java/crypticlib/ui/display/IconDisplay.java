package crypticlib.ui.display;

import crypticlib.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IconDisplay {

    protected @NotNull Material material;
    protected @Nullable String name;
    protected @Nullable List<String> lore;
    protected @Nullable @ApiStatus.AvailableSince("1.14") Integer customModelData;
    protected @Nullable @ApiStatus.AvailableSince("1.21.2") NamespacedKey itemModel;

    public IconDisplay(@NotNull Material material) {
        this.material = material;
    }

    public @NotNull Material material() {
        return material;
    }

    public IconDisplay setMaterial(@NotNull Material material) {
        this.material = material;
        return this;
    }

    public @Nullable String name() {
        return name;
    }

    public IconDisplay setName(@Nullable String name) {
        this.name = name;
        return this;
    }

    public @Nullable List<String> lore() {
        return lore;
    }

    public IconDisplay setLore(@Nullable List<String> lore) {
        this.lore = lore;
        return this;
    }

    @ApiStatus.AvailableSince("1.14")
    public Integer customModelData() {
        return customModelData;
    }

    @ApiStatus.AvailableSince("1.14")
    public IconDisplay setCustomModelData(@Nullable Integer customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    @ApiStatus.AvailableSince("1.21.2")
    public @Nullable NamespacedKey itemModel() {
        return itemModel;
    }

    @ApiStatus.AvailableSince("1.21.2")
    public IconDisplay setItemModel(@Nullable NamespacedKey itemModel) {
        this.itemModel = itemModel;
        return this;
    }

    public @NotNull ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(material);
        return applyToItemStack(itemStack);
    }

    /**
     * 将除物品类型外的参数应用到某个物品上
     * @param itemStack 原始物品
     * @return 修改后的物品
     */
    public @NotNull ItemStack applyToItemStack(@NotNull ItemStack itemStack) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta == null)
            return itemStack;
        if (name != null)
            itemMeta.setDisplayName(name);
        if (lore != null)
            itemMeta.setLore(lore);
        MinecraftVersion current = MinecraftVersion.current();
        if (current.afterOrEquals(MinecraftVersion.V1_14)) {
            if (customModelData != null)
                itemMeta.setCustomModelData(customModelData);
        }
        if (current.afterOrEquals(MinecraftVersion.V1_21_2)) {
            itemMeta.setItemModel(itemModel);
        }
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
