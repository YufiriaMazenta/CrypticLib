package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.NbtTagLongArray;
import net.minecraft.server.v1_13_R1.NBTTagLongArray;

public class V1_13_R1NbtTagLongArray extends NbtTagLongArray {

    public V1_13_R1NbtTagLongArray(long[] value) {
        super(value);
    }

    public V1_13_R1NbtTagLongArray(Object nmsNbtLongArray) {
        super(nmsNbtLongArray);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagLongArray nbtTagLongArray = (NBTTagLongArray) nmsNbt;
        setValue(nbtTagLongArray.d());
    }

    @Override
    public NBTTagLongArray toNms() {
        return new NBTTagLongArray(value());
    }
}
