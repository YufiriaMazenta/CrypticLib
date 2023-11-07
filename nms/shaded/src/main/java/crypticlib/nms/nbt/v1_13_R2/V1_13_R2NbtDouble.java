package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.AbstractNbtDouble;
import net.minecraft.server.v1_13_R2.NBTTagDouble;

public class V1_13_R2NbtDouble extends AbstractNbtDouble {

    public V1_13_R2NbtDouble(double value) {
        super(value);
    }

    public V1_13_R2NbtDouble(Object nmsNbtDouble) {
        super(nmsNbtDouble);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagDouble nbtTagDouble = (NBTTagDouble) nmsNbt;
        setValue(nbtTagDouble.asDouble());
    }

    @Override
    public NBTTagDouble toNms() {
        return new NBTTagDouble(value());
    }

}
