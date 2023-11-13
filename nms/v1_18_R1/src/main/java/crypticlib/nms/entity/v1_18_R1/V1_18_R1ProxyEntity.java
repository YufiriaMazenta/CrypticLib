package crypticlib.nms.entity.v1_18_R1;

import crypticlib.nms.entity.ProxyEntity;
import crypticlib.nms.nbt.v1_18_R1.V1_18_R1NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_18_R1ProxyEntity extends ProxyEntity {

    public V1_18_R1ProxyEntity(Entity entity) {
        super(entity);
    }

    @Override
    public ProxyEntity saveToEntity() {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        nmsEntity.g((NBTTagCompound) nbtTagCompound().toNms());
        return this;
    }

    @Override
    public void fromNms() {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        nmsEntity.e(nmsNbt);
        this.setNbtTagCompound(new V1_18_R1NbtTagCompound(nmsNbt));
    }

}
