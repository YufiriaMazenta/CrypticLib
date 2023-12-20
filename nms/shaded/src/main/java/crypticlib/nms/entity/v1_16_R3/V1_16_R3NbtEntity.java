package crypticlib.nms.entity.v1_16_R3;

import crypticlib.nms.entity.NbtEntity;
import crypticlib.nms.nbt.v1_16_R3.V1_16_R3NbtTagCompound;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class V1_16_R3NbtEntity extends NbtEntity {

    public V1_16_R3NbtEntity(Entity entity) {
        super(entity);
    }

    @Override
    public NbtEntity saveNbtToEntity() {
        net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftEntity) bukkit).getHandle();
        nmsEntity.load((NBTTagCompound) nbtTagCompound.toNms());
        return this;
    }

    @Override
    public void fromBukkit() {
        net.minecraft.server.v1_16_R3.Entity nmsEntity = ((CraftEntity) bukkit).getHandle();
        NBTTagCompound nmsNbt = new NBTTagCompound();
        this.setNbtTagCompound(new V1_16_R3NbtTagCompound(nmsEntity.save(nmsNbt)));
    }

}
