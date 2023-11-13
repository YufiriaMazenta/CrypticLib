package crypticlib.nms.nbt.v1_20_R2;

import crypticlib.nms.nbt.NbtTagShort;
import net.minecraft.nbt.NBTTagShort;

public class V1_20_R2NbtTagShort extends NbtTagShort {

    public V1_20_R2NbtTagShort(short value) {
        super(value);
    }

    public V1_20_R2NbtTagShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.h());
    }

    @Override
    public NBTTagShort toNms() {
        return NBTTagShort.a(value());
    }
}
