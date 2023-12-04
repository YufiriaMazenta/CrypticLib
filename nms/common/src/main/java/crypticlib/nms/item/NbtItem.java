package crypticlib.nms.item;

import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.util.ItemUtil;
import crypticlib.util.ReflectUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

public abstract class NbtItem {

    private final ItemStack bukkit;
    private NbtTagCompound nbtTagCompound;

    public NbtItem(@NotNull ItemStack itemStack) {
        if (ItemUtil.isAir(itemStack)) {
            throw new IllegalArgumentException("Can not create an air item");
        }
        this.bukkit = itemStack;
        loadNbtFromBukkit();
    }

    public NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound) {
        this(material, nbtTagCompound, 1);
    }

    public NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound, @NotNull Integer amount) {
        this.bukkit = new ItemStack(material, amount);
        this.nbtTagCompound = nbtTagCompound;
    }

    protected ItemStack bukkit() {
        return bukkit;
    }

    public Integer amount() {
        return bukkit.getAmount();
    }

    public NbtItem setAmount(@NotNull Integer amount) {
        bukkit.setAmount(amount);
        return this;
    }

    public Material material() {
        return bukkit.getType();
    }

    public NbtItem setMaterial(@NotNull Material material) {
        this.bukkit.setType(material);
        return this;
    }

    public NbtTagCompound nbtTagCompound() {
        return nbtTagCompound;
    }

    public NbtItem setNbtTagCompound(NbtTagCompound nbtTagCompound) {
        this.nbtTagCompound = nbtTagCompound;
        return this;
    }

    public abstract void loadNbtFromBukkit();

    /**
     * 将NBT保存到物品上,同时返回一个物品的克隆
     * @return 物品的克隆
     */
    public abstract ItemStack saveNbtToBukkit();

}
