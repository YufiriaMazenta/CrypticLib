package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.AbstractNbtInt;
import net.minecraft.server.v1_16_R3.NBTTagInt;

public class V1_16_R3NbtInt extends AbstractNbtInt {

    public V1_16_R3NbtInt(int value) {
        super(value);
    }

    public V1_16_R3NbtInt(Object nmsNbtInt) {
        super(nmsNbtInt);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagInt nbtTagInt = (NBTTagInt) nmsNbt;
        setValue(nbtTagInt.asInt());
    }

    @Override
    public NBTTagInt toNms() {
        return NBTTagInt.a(value());
    }
}
