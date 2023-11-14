package crypticlib.nms.entity.v1_16_R2;

import crypticlib.nms.entity.ProxyEntity;
import crypticlib.nms.nbt.v1_16_R2.V1_16_R2NbtTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_16_R2ProxyEntity extends ProxyEntity {

    public V1_16_R2ProxyEntity(Entity entity) {
        super(entity);
    }

    @Override
    public ProxyEntity saveNbtToEntity() {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        nmsEntity.load((NBTTagCompound) nbtTagCompound().toNms());
        return this;
    }

    @Override
    public void fromNms() {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        this.setNbtTagCompound(new V1_16_R2NbtTagCompound(nmsEntity.save(nmsNbt)));
    }

}
