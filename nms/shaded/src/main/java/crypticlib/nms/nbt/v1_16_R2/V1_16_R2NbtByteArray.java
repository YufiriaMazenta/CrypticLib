package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.AbstractNbtByteArray;
import net.minecraft.server.v1_16_R2.NBTTagByteArray;

public class V1_16_R2NbtByteArray extends AbstractNbtByteArray {

    public V1_16_R2NbtByteArray(byte[] value) {
        super(value);
    }

    public V1_16_R2NbtByteArray(Object object) {
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
