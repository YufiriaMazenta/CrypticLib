package crypticlib.nms.nbt.v1_19_R1;

import crypticlib.nms.nbt.AbstractNbtInt;
import net.minecraft.nbt.NBTTagInt;

public class V1_19_R1NbtInt extends AbstractNbtInt {

    public V1_19_R1NbtInt(int value) {
        super(value);
    }

    public V1_19_R1NbtInt(Object nmsNbtInt) {
        super(nmsNbtInt);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagInt nbtTagInt = (NBTTagInt) nmsNbt;
        setValue(nbtTagInt.f());
    }

    @Override
    public NBTTagInt toNms() {
        return NBTTagInt.a(value());
    }
}
