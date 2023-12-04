package crypticlib.nms.entity.v1_19_R2;

import crypticlib.nms.entity.NbtEntity;
import crypticlib.nms.nbt.v1_19_R2.V1_19_R2NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_19_R2NbtEntity extends NbtEntity {

    public V1_19_R2NbtEntity(Entity entity) {
        super(entity);
    }

    @Override
    public NbtEntity saveNbtToEntity() {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        nmsEntity.g((NBTTagCompound) nbtTagCompound().toNms());
        return this;
    }

    @Override
    public void loadFromBukkit() {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        this.setNbtTagCompound(new V1_19_R2NbtTagCompound(nmsEntity.f(nmsNbt)));
    }

}
