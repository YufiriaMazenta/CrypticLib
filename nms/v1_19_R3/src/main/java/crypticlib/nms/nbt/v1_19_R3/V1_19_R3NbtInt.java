package crypticlib.nms.nbt.v1_19_R3;

import crypticlib.nms.nbt.AbstractNbtInt;
import net.minecraft.nbt.NBTTagInt;

public class V1_19_R3NbtInt extends AbstractNbtInt {

    public V1_19_R3NbtInt(int value) {
        super(value);
    }

    public V1_19_R3NbtInt(Object nmsNbtInt) {
        super(nmsNbtInt);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagInt nbtTagInt = (NBTTagInt) nmsNbt;
        setValue(nbtTagInt.g());
    }

    @Override
    public NBTTagInt toNms() {
        return NBTTagInt.a(value());
    }
}
