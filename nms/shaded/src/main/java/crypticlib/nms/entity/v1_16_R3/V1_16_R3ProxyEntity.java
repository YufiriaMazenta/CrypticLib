package crypticlib.nms.entity.v1_16_R3;

import crypticlib.nms.entity.ProxyEntity;
import crypticlib.nms.nbt.v1_16_R3.V1_16_R3NbtTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_16_R3ProxyEntity extends ProxyEntity {

    public V1_16_R3ProxyEntity(Entity entity) {
        super(entity);
    }

    @Override
    public ProxyEntity saveToEntity() {
        net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        nmsEntity.load((NBTTagCompound) nbtTagCompound().toNms());
        return this;
    }

    @Override
    public void fromNms() {
        net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        nmsEntity.d(nmsNbt);
        this.setNbtTagCompound(new V1_16_R3NbtTagCompound(nmsNbt));
    }

}
