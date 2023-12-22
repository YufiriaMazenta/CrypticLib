package crypticlib.nms.tile.v1_20_R2;

import crypticlib.nms.nbt.v1_20_R2.V1_20_R2NbtTagCompound;
import crypticlib.nms.tile.NbtTileEntity;
import crypticlib.util.ReflectUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.level.block.entity.TileEntity;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftBlockEntityState;

import java.lang.reflect.Method;

public class V1_20_R2NbtTileEntity extends NbtTileEntity {

    public V1_20_R2NbtTileEntity(BlockState bukkit) {
        super(bukkit);
    }

    @Override
    public void saveNbtToTileEntity() {
        CraftBlockEntityState<?> craftTileEntity = ((CraftBlockEntityState<?>) bukkit);
        Method getSnapshotMethod = ReflectUtil.getDeclaredMethod(CraftBlockEntityState.class, "getSnapshot");
        TileEntity nmsTileEntity = (TileEntity) ReflectUtil.invokeDeclaredMethod(getSnapshotMethod, craftTileEntity);
        nmsTileEntity.a((NBTTagCompound) nbtTagCompound.toNms());
    }

    @Override
    public void fromBukkit() {
        NBTTagCompound nms = ((CraftBlockEntityState<?>) bukkit).getSnapshotNBT();
        setNbtTagCompound(new V1_20_R2NbtTagCompound(nms));
    }
}
