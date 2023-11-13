package crypticlib.nms.entity.v1_12_R1;

import crypticlib.nms.entity.ProxyEntity;
import crypticlib.nms.nbt.v1_12_R1.V1_12_R1NbtTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_12_R1ProxyEntity extends ProxyEntity {

    public V1_12_R1ProxyEntity(Entity entity) {
        super(entity);
    }

    @Override
    public ProxyEntity saveToEntity() {
        net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        nmsEntity.f((NBTTagCompound) nbtTagCompound().toNms());
        return this;
    }

    @Override
    public void fromNms() {
        net.minecraft.server.v1_12_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        this.setNbtTagCompound(new V1_12_R1NbtTagCompound(nmsEntity.save(nmsNbt)));

    }

}