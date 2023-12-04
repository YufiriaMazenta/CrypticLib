package crypticlib.nms.item.v1_19_R3;

import crypticlib.nms.item.NbtItem;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_19_R3.V1_19_R3NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class V1_19_R3NbtItem extends NbtItem {

    public V1_19_R3NbtItem(@NotNull ItemStack itemStack) {
        super(itemStack);
    }

    public V1_19_R3NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound) {
        super(material, nbtTagCompound);
    }

    public V1_19_R3NbtItem(@NotNull Material material, @NotNull NbtTagCompound nbtTagCompound, @NotNull Integer amount) {
        super(material, nbtTagCompound, amount);
    }

    @Override
    public void loadNbtFromBukkit() {
        setNbtTagCompound(new V1_19_R3NbtTagCompound(CraftItemStack.asNMSCopy(bukkit()).v()));
    }

    @Override
    public ItemStack saveNbtToBukkit() {
        NBTTagCompound nbtTagCompound = (NBTTagCompound) nbtTagCompound().toNms();
        ItemStack tmpItem = new ItemStack(bukkit().getType());
        net.minecraft.world.item.ItemStack nmsCopy = CraftItemStack.asNMSCopy(tmpItem);
        nmsCopy.c(nbtTagCompound);
        bukkit().setItemMeta(CraftItemStack.getItemMeta(nmsCopy));
        return bukkit();
    }

}
