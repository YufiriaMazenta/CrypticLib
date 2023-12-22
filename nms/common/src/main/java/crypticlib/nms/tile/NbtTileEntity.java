package crypticlib.nms.tile;

import crypticlib.nms.nbt.NbtTagCompound;
import org.bukkit.block.BlockState;
import org.bukkit.block.TileState;

public abstract class NbtTileEntity {

    protected BlockState bukkit;
    protected NbtTagCompound nbtTagCompound;

    public NbtTileEntity(BlockState bukkit) {
        this.bukkit = bukkit;
        fromBukkit();
    }

    public BlockState bukkit() {
        return bukkit;
    }

    public void setBukkit(TileState bukkit) {
        this.bukkit = bukkit;
    }

    public NbtTagCompound nbtTagCompound() {
        return nbtTagCompound;
    }

    public void setNbtTagCompound(NbtTagCompound nbtTagCompound) {
        this.nbtTagCompound = nbtTagCompound;
    }

    public abstract void saveNbtToTileEntity();

    public abstract void fromBukkit();

}
