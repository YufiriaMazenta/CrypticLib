package crypticlib.nms.tile.v1_16_R3;

import crypticlib.nms.nbt.v1_16_R3.V1_16_R3NbtTagCompound;
import crypticlib.nms.tile.NbtTileEntity;
import crypticlib.util.ReflectUtil;
import net.minecraft.server.v1_16_R3.NBTTagCompound;
import net.minecraft.server.v1_16_R3.TileEntity;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_16_R3.block.CraftBlockEntityState;

import java.lang.reflect.Method;

public class V1_16_R3NbtTileEntity extends NbtTileEntity {

    public V1_16_R3NbtTileEntity(BlockState bukkit) {
        super(bukkit);
    }

    @Override
    public void saveNbtToTileEntity() {
        CraftBlockEntityState<?> craftTileEntity = ((CraftBlockEntityState<?>) bukkit);
        Method getSnapshotMethod = ReflectUtil.getDeclaredMethod(CraftBlockEntityState.class, "getSnapshot");
        TileEntity nmsTileEntity = (TileEntity) ReflectUtil.invokeDeclaredMethod(getSnapshotMethod, craftTileEntity);
        nmsTileEntity.load(nmsTileEntity.getBlock(), (NBTTagCompound) nbtTagCompound.toNms());
    }

    @Override
    public void fromBukkit() {
        NBTTagCompound nms = ((CraftBlockEntityState<?>) bukkit).getSnapshotNBT();
        setNbtTagCompound(new V1_16_R3NbtTagCompound(nms));
    }
}
