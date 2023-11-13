package crypticlib.nms.nbt.v1_17_R1;

import crypticlib.nms.nbt.NbtTagInt;
import net.minecraft.nbt.NBTTagInt;

public class V1_17_R1NbtTagInt extends NbtTagInt {

    public V1_17_R1NbtTagInt(int value) {
        super(value);
    }

    public V1_17_R1NbtTagInt(Object nmsNbtInt) {
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
