package crypticlib.ui.display;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class IconDisplay {

    protected @NotNull Material material;
    protected @Nullable String name;
    protected @Nullable List<String> lore;
    protected @Nullable Integer customModelData;
    //内部参数,请勿随意修改
    protected @Nullable ItemStack display;

    public IconDisplay(@NotNull Material material) {
        this(material, null, null, null);
    }

    public IconDisplay(@NotNull Material material, @Nullable List<String> lore, @Nullable Integer customModelData) {
        this(material, null, lore, customModelData);
    }

    public IconDisplay(@NotNull Material material, @Nullable String name, @Nullable Integer customModelData) {
        this(material, name, null, customModelData);
    }

    public IconDisplay(@NotNull Material material, @Nullable Integer customModelData) {
        this(material, null, null, customModelData);
    }

    public IconDisplay(@NotNull Material material, @Nullable List<String> lore) {
        this(material, null, lore, null);
    }

    public IconDisplay(@NotNull Material material, @Nullable String name) {
        this(material, name, null, null);
    }

    public IconDisplay(@NotNull Material material, @Nullable String name, @Nullable List<String> lore) {
        this(material, name, lore, null);
    }

    public IconDisplay(@NotNull Material material, @Nullable String name, @Nullable List<String> lore, @Nullable Integer customModelData) {
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.customModelData = customModelData;
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

    public Integer customModelData() {
        return customModelData;
    }

    public IconDisplay setCustomModelData(@Nullable Integer customModelData) {
        this.customModelData = customModelData;
        return this;
    }

    public @NotNull ItemStack display() {
        if (display == null) {
            display = toItemStack();
        }
        return display;
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
        if (customModelData != null)
            itemMeta.setCustomModelData(customModelData);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

}
