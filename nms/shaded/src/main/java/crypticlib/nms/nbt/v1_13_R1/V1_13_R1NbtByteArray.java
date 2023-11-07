package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.AbstractNbtByteArray;
import net.minecraft.server.v1_13_R1.NBTTagByteArray;

public class V1_13_R1NbtByteArray extends AbstractNbtByteArray {

    public V1_13_R1NbtByteArray(byte[] value) {
        super(value);
    }

    public V1_13_R1NbtByteArray(Object object) {
        super(object);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagByteArray nbtTagByteArray = (NBTTagByteArray) nmsNbt;
        setValue(nbtTagByteArray.c());
    }

    @Override
    public NBTTagByteArray toNms() {
        return new NBTTagByteArray(value());
    }

}
