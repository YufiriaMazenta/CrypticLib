package crypticlib.nms.item.v1_17_R1;

import crypticlib.nms.item.NbtItem;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_17_R1.V1_17_R1NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class V1_17_R1NbtItem extends NbtItem {

    public V1_17_R1NbtItem(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    public V1_17_R1NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound) {
        super(material, nbtTagCompound);
    }

    public V1_17_R1NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound, @NotNull Integer amount) {
        super(material, nbtTagCompound, amount);
    }

    @Override
    public void fromBukkit() {
        setNbtTagCompound(new V1_17_R1NbtTagCompound(CraftItemStack.asNMSCopy(bukkit).getOrCreateTag()));
    }

    @Override
    public ItemStack saveNbtToItem() {
        NBTTagCompound nms = (NBTTagCompound) nbtTagCompound.toNms();
        ItemStack tmpItem = new ItemStack(bukkit.getType());
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(tmpItem);
        nmsCopy.setTag(nms);
        bukkit.setItemMeta(CraftItemStack.getItemMeta(nmsCopy));
        return bukkit;
    }

    @Override
    public NbtItem clone() {
        NbtItem clone = new V1_17_R1NbtItem(this.bukkit.clone());
        clone.setNbtTagCompound(nbtTagCompound.clone());
        return clone;
    }


}
