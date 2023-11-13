package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.NbtTagIntArray;
import net.minecraft.server.v1_16_R3.NBTTagIntArray;

public class V1_16_R3NbtTagIntArray extends NbtTagIntArray {

    public V1_16_R3NbtTagIntArray(int[] value) {
        super(value);
    }

    public V1_16_R3NbtTagIntArray(Object nmsNbtIntArray) {
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
