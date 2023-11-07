package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.AbstractNbtIntArray;
import net.minecraft.server.v1_13_R2.NBTTagIntArray;

public class V1_13_R2NbtIntArray extends AbstractNbtIntArray {

    public V1_13_R2NbtIntArray(int[] value) {
        super(value);
    }

    public V1_13_R2NbtIntArray(Object nmsNbtIntArray) {
        super(nmsNbtIntArray);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagIntArray nbtTagIntArray = (NBTTagIntArray) nmsNbt;
        setValue(nbtTagIntArray.d());
    }

    @Override
    public NBTTagIntArray toNms() {
        return new NBTTagIntArray(value());
    }

}
