package crypticlib.nms.nbt.v1_20_R2;

import crypticlib.nms.nbt.NbtTagDouble;
import net.minecraft.nbt.NBTTagDouble;

public class V1_20_R2NbtTagDouble extends NbtTagDouble {

    public V1_20_R2NbtTagDouble(double value) {
        super(value);
    }

    public V1_20_R2NbtTagDouble(Object nmsNbtDouble) {
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
