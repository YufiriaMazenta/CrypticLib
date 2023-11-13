package crypticlib.nms.nbt.v1_17_R1;

import crypticlib.nms.nbt.NbtTagShort;
import net.minecraft.nbt.NBTTagShort;

public class V1_17_R1NbtTagShort extends NbtTagShort {

    public V1_17_R1NbtTagShort(short value) {
        super(value);
    }

    public V1_17_R1NbtTagShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.asShort());
    }

    @Override
    public NBTTagShort toNms() {
        return NBTTagShort.a(value());
    }
}
