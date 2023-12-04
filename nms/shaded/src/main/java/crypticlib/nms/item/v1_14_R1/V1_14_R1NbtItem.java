package crypticlib.nms.item.v1_14_R1;

import crypticlib.nms.item.NbtItem;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_14_R1.V1_14_R1NbtTagCompound;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class V1_14_R1NbtItem extends NbtItem {

    public V1_14_R1NbtItem(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    public V1_14_R1NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound) {
        super(material, nbtTagCompound);
    }

    public V1_14_R1NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound, @NotNull Integer amount) {
        super(material, nbtTagCompound, amount);
    }

    @Override
    public void loadNbtFromBukkit() {
        setNbtTagCompound(new V1_14_R1NbtTagCompound(CraftItemStack.asNMSCopy(bukkit()).getOrCreateTag()));
    }

    @Override
    public ItemStack saveNbtToBukkit() {
        NBTTagCompound nbtTagCompound = (NBTTagCompound) nbtTagCompound().toNms();
        ItemStack tmpItem = new ItemStack(bukkit().getType());
        net.minecraft.server.v1_14_R1.ItemStack nmsCopy = CraftItemStack.asNMSCopy(tmpItem);
        nmsCopy.setTag(nbtTagCompound);
        bukkit().setItemMeta(CraftItemStack.getItemMeta(nmsCopy));
        return bukkit();
    }

}
