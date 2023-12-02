package crypticlib.nms.item.v1_16_R2;

import crypticlib.nms.item.Item;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.nms.nbt.v1_16_R2.V1_16_R2NbtTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class V1_16_R2Item extends Item {

    public V1_16_R2Item(ItemStack itemStack) {
        super(itemStack.getType().name(), new V1_16_R2NbtTagCompound(CraftItemStack.asNMSCopy(itemStack).getOrCreateTag()), itemStack.getAmount());
    }

    public V1_16_R2Item(String material, NbtTagCompound nbtTagCompound) {
        super(material, nbtTagCompound);
    }

    public V1_16_R2Item(String material, NbtTagCompound nbtTagCompound, Integer amount) {
        super(material, nbtTagCompound, amount);
    }

    @Override
    public @NotNull ItemStack buildBukkit() {
        return CraftItemStack.asBukkitCopy(buildNMS());
    }

    @Override
    public net.minecraft.server.v1_16_R2.@NotNull ItemStack buildNMS() {
        Material type = Material.matchMaterial(material());
        if (type == null) {
            throw new IllegalArgumentException(material() + " is an undefined item");
        }
        ItemStack item = new ItemStack(type, amount());
        net.minecraft.server.v1_16_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.setTag((NBTTagCompound) nbtTagCompound().toNms());
        return nms;
    }

}
