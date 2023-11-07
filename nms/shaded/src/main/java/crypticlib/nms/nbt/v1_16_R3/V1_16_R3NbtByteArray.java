package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.AbstractNbtByteArray;
import net.minecraft.server.v1_16_R3.NBTTagByteArray;

public class V1_16_R3NbtByteArray extends AbstractNbtByteArray {

    public V1_16_R3NbtByteArray(byte[] value) {
        super(value);
    }

    public V1_16_R3NbtByteArray(Object object) {
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
