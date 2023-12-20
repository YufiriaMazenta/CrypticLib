package crypticlib.nms.entity.v1_16_R1;

import crypticlib.nms.entity.NbtEntity;
import crypticlib.nms.nbt.v1_16_R1.V1_16_R1NbtTagCompound;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_16_R1NbtEntity extends NbtEntity {

    public V1_16_R1NbtEntity(Entity entity) {
        super(entity);
    }

    @Override
    public NbtEntity saveNbtToEntity() {
        net.minecraft.server.v1_16_R1.Entity nmsEntity = ((CraftEntity) bukkit).getHandle();
        nmsEntity.load((NBTTagCompound) nbtTagCompound.toNms());
        return this;
    }

    @Override
    public void fromBukkit() {
        net.minecraft.server.v1_16_R1.Entity nmsEntity = ((CraftEntity) bukkit).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        this.setNbtTagCompound(new V1_16_R1NbtTagCompound(nmsEntity.save(nmsNbt)));
    }

}
