package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.AbstractNbtShort;
import net.minecraft.server.v1_13_R2.NBTTagShort;

public class V1_13_R2NbtShort extends AbstractNbtShort {

    public V1_13_R2NbtShort(short value) {
        super(value);
    }

    public V1_13_R2NbtShort(Object nmsNbtShort) {
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
