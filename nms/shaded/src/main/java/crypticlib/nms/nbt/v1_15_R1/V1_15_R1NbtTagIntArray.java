package crypticlib.nms.nbt.v1_15_R1;

import crypticlib.nms.nbt.NbtTagIntArray;
import net.minecraft.server.v1_15_R1.NBTTagIntArray;

public class V1_15_R1NbtTagIntArray extends NbtTagIntArray {

    public V1_15_R1NbtTagIntArray(int[] value) {
        super(value);
    }

    public V1_15_R1NbtTagIntArray(Object nmsNbtIntArray) {
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
