package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.NbtTagInt;
import net.minecraft.server.v1_16_R2.NBTTagInt;

public class V1_16_R2NbtTagInt extends NbtTagInt {

    public V1_16_R2NbtTagInt(int value) {
        super(value);
    }

    public V1_16_R2NbtTagInt(Object nmsNbtInt) {
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
