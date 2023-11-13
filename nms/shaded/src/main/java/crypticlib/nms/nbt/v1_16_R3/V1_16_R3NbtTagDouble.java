package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.NbtTagDouble;
import net.minecraft.server.v1_16_R3.NBTTagDouble;

public class V1_16_R3NbtTagDouble extends NbtTagDouble {

    public V1_16_R3NbtTagDouble(double value) {
        super(value);
    }

    public V1_16_R3NbtTagDouble(Object nmsNbtDouble) {
        super(nmsNbtDouble);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagDouble nbtTagDouble = (NBTTagDouble) nmsNbt;
        setValue(nbtTagDouble.asDouble());
    }

    @Override
    public NBTTagDouble toNms() {
        return NBTTagDouble.a(value());
    }

}
