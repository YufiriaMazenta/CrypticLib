package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.AbstractNbtIntArray;
import net.minecraft.server.v1_16_R3.NBTTagIntArray;

public class V1_16_R3NbtIntArray extends AbstractNbtIntArray {

    public V1_16_R3NbtIntArray(int[] value) {
        super(value);
    }

    public V1_16_R3NbtIntArray(Object nmsNbtIntArray) {
        super(nmsNbtIntArray);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagIntArray nbtTagIntArray = (NBTTagIntArray) nmsNbt;
        setValue(nbtTagIntArray.getInts());
    }

    @Override
    public NBTTagIntArray toNms() {
        return new NBTTagIntArray(value());
    }
}
