package crypticlib.nms.item.v1_16_R3;

import crypticlib.nms.item.Item;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_16_R3.V1_16_R3NbtTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class V1_16_R3Item extends Item {

    public V1_16_R3Item(ItemStack itemStack) {
        super(itemStack.getType().name(), new V1_16_R3NbtTagCompound(CraftItemStack.asNMSCopy(itemStack).getOrCreateTag()));
    }

    public V1_16_R3Item(String material, NbtTagCompound nbtTagCompound) {
        super(material, nbtTagCompound);
    }

    @Override
    public ItemStack buildBukkit() {
        return CraftItemStack.asBukkitCopy(buildNMS());
    }

    @Override
    public net.minecraft.server.v1_16_R3.ItemStack buildNMS() {
        Material type = Material.matchMaterial(material());
        if (type == null) {
            throw new IllegalArgumentException(material() + " is an undefined item");
        }
        ItemStack item = new ItemStack(type);
        net.minecraft.server.v1_16_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.setTag((NBTTagCompound) nbtTagCompound().toNms());
        return nms;
    }

}
