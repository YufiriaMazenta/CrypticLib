package crypticlib.nms.entity.v1_17_R1;

import crypticlib.nms.entity.NbtEntity;
import crypticlib.nms.nbt.v1_17_R1.V1_17_R1NbtTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_17_R1NbtEntity extends NbtEntity {

    public V1_17_R1NbtEntity(Entity entity) {
        super(entity);
    }

    @Override
    public NbtEntity saveNbtToEntity() {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        nmsEntity.load((NBTTagCompound) nbtTagCompound().toNms());
        return this;
    }

    @Override
    public void loadFromBukkit() {
        net.minecraft.world.entity.Entity nmsEntity = ((CraftEntity) bukkitEntity()).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        this.setNbtTagCompound(new V1_17_R1NbtTagCompound(nmsEntity.save(nmsNbt)));
    }

}
