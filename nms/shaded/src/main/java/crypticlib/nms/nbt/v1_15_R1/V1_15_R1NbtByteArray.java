package crypticlib.nms.nbt.v1_15_R1;

import crypticlib.nms.nbt.AbstractNbtByteArray;
import net.minecraft.server.v1_15_R1.NBTTagByteArray;

public class V1_15_R1NbtByteArray extends AbstractNbtByteArray {

    public V1_15_R1NbtByteArray(byte[] value) {
        super(value);
    }

    public V1_15_R1NbtByteArray(Object object) {
        super(object);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagByteArray nbtTagByteArray = (NBTTagByteArray) nmsNbt;
        setValue(nbtTagByteArray.getBytes());
    }

    @Override
    public NBTTagByteArray toNms() {
        return new NBTTagByteArray(value());
    }

}
