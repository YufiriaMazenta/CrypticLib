package crypticlib.nms.nbt.v1_17_R1;

import crypticlib.nms.nbt.NbtTagDouble;
import net.minecraft.nbt.NBTTagDouble;

public class V1_17_R1NbtTagDouble extends NbtTagDouble {

    public V1_17_R1NbtTagDouble(double value) {
        super(value);
    }

    public V1_17_R1NbtTagDouble(Object nmsNbtDouble) {
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
