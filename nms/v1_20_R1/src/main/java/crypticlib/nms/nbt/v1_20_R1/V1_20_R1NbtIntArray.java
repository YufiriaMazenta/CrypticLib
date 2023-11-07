package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.AbstractNbtIntArray;
import net.minecraft.nbt.NBTTagIntArray;

public class V1_20_R1NbtIntArray extends AbstractNbtIntArray {

    public V1_20_R1NbtIntArray(int[] value) {
        super(value);
    }

    public V1_20_R1NbtIntArray(Object nmsNbtIntArray) {
        super(nmsNbtIntArray);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagIntArray nbtTagIntArray = (NBTTagIntArray) nmsNbt;
        setValue(nbtTagIntArray.g());
    }

    @Override
    public NBTTagIntArray toNms() {
        return new NBTTagIntArray(value());
    }
}
