package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.AbstractNbtShort;
import net.minecraft.server.v1_13_R1.NBTTagShort;

public class V1_13_R1NbtShort extends AbstractNbtShort {

    public V1_13_R1NbtShort(short value) {
        super(value);
    }

    public V1_13_R1NbtShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.f());
    }

    @Override
    public NBTTagShort toNms() {
        return new NBTTagShort(value());
    }
}
