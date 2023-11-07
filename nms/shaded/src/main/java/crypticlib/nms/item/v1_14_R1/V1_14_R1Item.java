package crypticlib.nms.item.v1_14_R1;

import crypticlib.nms.item.Item;
import crypticlib.nms.nbt.AbstractNbtCompound;
import crypticlib.nms.nbt.v1_14_R1.V1_14_R1NbtCompound;
import net.minecraft.server.v1_14_R1.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_14_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class V1_14_R1Item implements Item {

    private String material;
    private AbstractNbtCompound nbtCompound;

    public V1_14_R1Item(ItemStack itemStack) {
        this.material = itemStack.getType().name();
        this.nbtCompound = new V1_14_R1NbtCompound(CraftItemStack.asNMSCopy(itemStack).getOrCreateTag());
    }

    public V1_14_R1Item(String material, AbstractNbtCompound nbtCompound) {
        this.material = material;
        this.nbtCompound = nbtCompound;
    }

    @Override
    public String material() {
        return this.material;
    }

    @Override
    public void setMaterial(String material) {
        this.material = material;
    }

    @Override
    public AbstractNbtCompound nbtCompound() {
        return nbtCompound;
    }

    @Override
    public void setNbtCompound(AbstractNbtCompound nbtCompound) {
        this.nbtCompound = nbtCompound;
    }

    @Override
    public ItemStack buildBukkit() {
        return CraftItemStack.asBukkitCopy(buildNMS());
    }

    @Override
    public net.minecraft.server.v1_14_R1.ItemStack buildNMS() {
        Material type = Material.matchMaterial(material);
        if (type == null) {
            throw new IllegalArgumentException(material + " is an undefined item");
        }
        ItemStack item = new ItemStack(type);
        net.minecraft.server.v1_14_R1.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.setTag((NBTTagCompound) nbtCompound.toNms());
        return nms;
    }

}
