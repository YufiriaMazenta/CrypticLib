package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.AbstractNbtDouble;
import net.minecraft.nbt.NBTTagDouble;

public class V1_20_R1NbtDouble extends AbstractNbtDouble {

    public V1_20_R1NbtDouble(double value) {
        super(value);
    }

    public V1_20_R1NbtDouble(Object nmsNbtDouble) {
        super(nmsNbtDouble);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagDouble nbtTagDouble = (NBTTagDouble) nmsNbt;
        setValue(nbtTagDouble.j());
    }

    @Override
    public NBTTagDouble toNms() {
        return NBTTagDouble.a(value());
    }

}
