package crypticlib.nms.item.v1_16_R3;

import crypticlib.nms.item.Item;
import crypticlib.nms.nbt.AbstractNbtTagCompound;
import crypticlib.nms.nbt.v1_16_R3.V1_16_R3NbtTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class V1_16_R3Item implements Item {

    private String material;
    private AbstractNbtTagCompound nbtCompound;

    public V1_16_R3Item(ItemStack itemStack) {
        this.material = itemStack.getType().name();
        this.nbtCompound = new V1_16_R3NbtTagCompound(CraftItemStack.asNMSCopy(itemStack).getOrCreateTag());
    }

    public V1_16_R3Item(String material, AbstractNbtTagCompound nbtCompound) {
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
    public AbstractNbtTagCompound nbtTagCompound() {
        return nbtCompound;
    }

    @Override
    public void setNbtTagCompound(AbstractNbtTagCompound nbtCompound) {
        this.nbtCompound = nbtCompound;
    }

    @Override
    public ItemStack buildBukkit() {
        return CraftItemStack.asBukkitCopy(buildNMS());
    }

    @Override
    public net.minecraft.server.v1_16_R3.ItemStack buildNMS() {
        Material type = Material.matchMaterial(material);
        if (type == null) {
            throw new IllegalArgumentException(material + " is an undefined item");
        }
        ItemStack item = new ItemStack(type);
        net.minecraft.server.v1_16_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.setTag((NBTTagCompound) nbtCompound.toNms());
        return nms;
    }

}
