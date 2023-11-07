package crypticlib.nms.nbt.v1_19_R1;

import crypticlib.nms.nbt.AbstractNbtShort;
import net.minecraft.nbt.NBTTagShort;

public class V1_19_R1NbtShort extends AbstractNbtShort {

    public V1_19_R1NbtShort(short value) {
        super(value);
    }

    public V1_19_R1NbtShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.g());
    }

    @Override
    public NBTTagShort toNms() {
        return NBTTagShort.a(value());
    }
}
