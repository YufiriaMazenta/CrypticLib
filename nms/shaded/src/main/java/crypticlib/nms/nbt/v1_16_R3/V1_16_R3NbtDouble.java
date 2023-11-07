package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.AbstractNbtDouble;
import net.minecraft.server.v1_16_R3.NBTTagDouble;

public class V1_16_R3NbtDouble extends AbstractNbtDouble {

    public V1_16_R3NbtDouble(double value) {
        super(value);
    }

    public V1_16_R3NbtDouble(Object nmsNbtDouble) {
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
