package crypticlib.nms.item.v1_16_R2;

import crypticlib.nms.item.NbtItem;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_16_R2.V1_16_R2NbtTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class V1_16_R2NbtItem extends NbtItem {

    public V1_16_R2NbtItem(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    public V1_16_R2NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound) {
        super(material, nbtTagCompound);
    }

    public V1_16_R2NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound, @NotNull Integer amount) {
        super(material, nbtTagCompound, amount);
    }

    @Override
    public void fromBukkit() {
        setNbtTagCompound(new V1_16_R2NbtTagCompound(CraftItemStack.asNMSCopy(bukkit).getOrCreateTag()));
    }

    @Override
    public ItemStack saveNbtToItem() {
        NBTTagCompound nms = (NBTTagCompound) nbtTagCompound.toNms();
        ItemStack tmpItem = new ItemStack(bukkit.getType());
        net.minecraft.server.v1_16_R2.ItemStack nmsCopy = CraftItemStack.asNMSCopy(tmpItem);
        nmsCopy.setTag(nms);
        bukkit.setItemMeta(CraftItemStack.getItemMeta(nmsCopy));
        return bukkit;
    }

}
