package crypticlib.nms.item.v1_19_R1;

import crypticlib.nms.item.Item;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_19_R1.V1_19_R1NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class V1_19_R1Item extends Item {

    public V1_19_R1Item(ItemStack itemStack) {
        super(itemStack.getType().name(), new V1_19_R1NbtTagCompound(CraftItemStack.asNMSCopy(itemStack).v()), itemStack.getAmount());
    }

    public V1_19_R1Item(String material, NbtTagCompound nbtTagCompound) {
        super(material, nbtTagCompound);
    }

    public V1_19_R1Item(String material, NbtTagCompound nbtTagCompound, Integer amount) {
        super(material, nbtTagCompound, amount);
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
        ItemStack item = new ItemStack(type, amount());
        net.minecraft.world.item.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.c((NBTTagCompound) nbtTagCompound().toNms());
        return nms;
    }

}
