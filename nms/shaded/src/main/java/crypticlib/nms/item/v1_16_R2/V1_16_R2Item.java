package crypticlib.nms.item.v1_16_R2;

import crypticlib.nms.item.Item;
import crypticlib.nms.nbt.AbstractNbtCompound;
import crypticlib.nms.nbt.v1_16_R2.V1_16_R2NbtCompound;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class V1_16_R2Item implements Item {

    private String material;
    private AbstractNbtCompound nbtCompound;

    public V1_16_R2Item(ItemStack itemStack) {
        this.material = itemStack.getType().name();
        this.nbtCompound = new V1_16_R2NbtCompound(CraftItemStack.asNMSCopy(itemStack).getOrCreateTag());
    }

    public V1_16_R2Item(String material, AbstractNbtCompound nbtCompound) {
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
    public net.minecraft.server.v1_16_R2.ItemStack buildNMS() {
        Material type = Material.matchMaterial(material);
        if (type == null) {
            throw new IllegalArgumentException(material + " is an undefined item");
        }
        ItemStack item = new ItemStack(type);
        net.minecraft.server.v1_16_R2.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.setTag((NBTTagCompound) nbtCompound.toNms());
        return nms;
    }

}
