package crypticlib.nms.entity.v1_16_R2;

import crypticlib.nms.entity.NbtEntity;
import crypticlib.nms.nbt.v1_16_R2.V1_16_R2NbtTagCompound;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_16_R2NbtEntity extends NbtEntity {

    public V1_16_R2NbtEntity(Entity entity) {
        super(entity);
    }

    @Override
    public NbtEntity saveNbtToEntity() {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        nmsEntity.load((NBTTagCompound) nbtTagCompound().toNms());
        return this;
    }

    @Override
    public void loadFromBukkit() {
        net.minecraft.server.v1_16_R2.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        this.setNbtTagCompound(new V1_16_R2NbtTagCompound(nmsEntity.save(nmsNbt)));
    }

}
