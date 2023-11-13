package crypticlib.nms.item.v1_19_R2;

import crypticlib.nms.item.Item;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_19_R2.V1_19_R2NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class V1_19_R2Item extends Item {

    public V1_19_R2Item(ItemStack itemStack) {
        super(itemStack.getType().name(), new V1_19_R2NbtTagCompound(CraftItemStack.asNMSCopy(itemStack).v()));
    }

    public V1_19_R2Item(String material, NbtTagCompound nbtTagCompound) {
        super(material, nbtTagCompound);
    }

    @Override
    public ItemStack buildBukkit() {
        return CraftItemStack.asBukkitCopy(buildNMS());
    }

    @Override
    public net.minecraft.world.item.ItemStack buildNMS() {
        Material type = Material.matchMaterial(material());
        if (type == null) {
            throw new IllegalArgumentException(material() + " is an undefined item");
        }
        ItemStack item = new ItemStack(type);
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.c((NBTTagCompound) nbtTagCompound().toNms());
        return nms;
    }

}
