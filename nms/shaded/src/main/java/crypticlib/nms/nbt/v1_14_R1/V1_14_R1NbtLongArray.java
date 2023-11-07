package crypticlib.nms.nbt.v1_14_R1;

import crypticlib.nms.nbt.AbstractNbtLongArray;
import net.minecraft.server.v1_14_R1.NBTTagLongArray;

public class V1_14_R1NbtLongArray extends AbstractNbtLongArray {

    public V1_14_R1NbtLongArray(long[] value) {
        super(value);
    }

    public V1_14_R1NbtLongArray(Object nmsNbtLongArray) {
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
