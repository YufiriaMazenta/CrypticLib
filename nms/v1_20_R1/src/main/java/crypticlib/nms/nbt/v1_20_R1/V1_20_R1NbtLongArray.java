package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.AbstractNbtLongArray;
import net.minecraft.nbt.NBTTagLongArray;

public class V1_20_R1NbtLongArray extends AbstractNbtLongArray {

    public V1_20_R1NbtLongArray(long[] value) {
        super(value);
    }

    public V1_20_R1NbtLongArray(Object nmsNbtLongArray) {
        super(nmsNbtLongArray);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagLongArray nbtTagLongArray = (NBTTagLongArray) nmsNbt;
        setValue(nbtTagLongArray.g());
    }

    @Override
    public NBTTagLongArray toNms() {
        return new NBTTagLongArray(value());
    }
}
