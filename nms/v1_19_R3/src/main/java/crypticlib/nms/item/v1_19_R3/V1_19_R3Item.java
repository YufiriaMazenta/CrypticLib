package crypticlib.nms.item.v1_19_R3;

import crypticlib.nms.item.Item;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_19_R3.V1_19_R3NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_19_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class V1_19_R3Item extends Item {

    public V1_19_R3Item(ItemStack itemStack) {
        super(itemStack.getType().name(), new V1_19_R3NbtTagCompound(CraftItemStack.asNMSCopy(itemStack).v()), itemStack.getAmount());
    }

    public V1_19_R3Item(String material, NbtTagCompound nbtCompound) {
        super(material, nbtCompound);
    }

    public V1_19_R3Item(String material, NbtTagCompound nbtTagCompound, Integer amount) {
        super(material, nbtTagCompound, amount);
    }

    @Override
    public @NotNull ItemStack buildBukkit() {
        return CraftItemStack.asBukkitCopy(buildNMS());
    }

    @Override
    public net.minecraft.world.item.@NotNull ItemStack buildNMS() {
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
