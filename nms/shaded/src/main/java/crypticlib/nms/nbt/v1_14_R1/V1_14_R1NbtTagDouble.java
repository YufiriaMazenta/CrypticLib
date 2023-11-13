package crypticlib.nms.nbt.v1_14_R1;

import crypticlib.nms.nbt.NbtTagDouble;
import net.minecraft.server.v1_14_R1.NBTTagDouble;

public class V1_14_R1NbtTagDouble extends NbtTagDouble {

    public V1_14_R1NbtTagDouble(double value) {
        super(value);
    }

    public V1_14_R1NbtTagDouble(Object nmsNbtDouble) {
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
