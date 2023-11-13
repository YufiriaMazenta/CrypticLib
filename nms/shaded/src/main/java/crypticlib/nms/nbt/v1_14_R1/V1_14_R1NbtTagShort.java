package crypticlib.nms.nbt.v1_14_R1;

import crypticlib.nms.nbt.NbtTagShort;
import net.minecraft.server.v1_14_R1.NBTTagShort;

public class V1_14_R1NbtTagShort extends NbtTagShort {

    public V1_14_R1NbtTagShort(short value) {
        super(value);
    }

    public V1_14_R1NbtTagShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.asShort());
    }

    @Override
    public NBTTagShort toNms() {
        return new NBTTagShort(value());
    }
}
