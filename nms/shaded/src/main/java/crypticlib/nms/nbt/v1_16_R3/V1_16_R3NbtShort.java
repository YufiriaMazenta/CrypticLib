package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.AbstractNbtShort;
import net.minecraft.server.v1_16_R3.NBTTagShort;

public class V1_16_R3NbtShort extends AbstractNbtShort {

    public V1_16_R3NbtShort(short value) {
        super(value);
    }

    public V1_16_R3NbtShort(Object nmsNbtShort) {
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
