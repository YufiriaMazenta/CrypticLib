package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.AbstractNbtLongArray;
import net.minecraft.server.v1_16_R3.NBTTagLongArray;

public class V1_16_R3NbtLongArray extends AbstractNbtLongArray {

    public V1_16_R3NbtLongArray(long[] value) {
        super(value);
    }

    public V1_16_R3NbtLongArray(Object nmsNbtLongArray) {
        super(nmsNbtLongArray);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagLongArray nbtTagLongArray = (NBTTagLongArray) nmsNbt;
        setValue(nbtTagLongArray.getLongs());
    }

    @Override
    public NBTTagLongArray toNms() {
        return new NBTTagLongArray(value());
    }
}
