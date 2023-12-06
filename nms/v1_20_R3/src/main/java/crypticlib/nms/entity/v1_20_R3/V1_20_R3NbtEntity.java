package crypticlib.nms.entity.v1_20_R3;

import crypticlib.nms.entity.NbtEntity;
import crypticlib.nms.nbt.v1_20_R3.V1_20_R3NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_20_R3NbtEntity extends NbtEntity {

    public V1_20_R3NbtEntity(Entity entity) {
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
        this.setNbtTagCompound(new V1_20_R3NbtTagCompound(nmsEntity.f(nmsNbt)));
    }

}
