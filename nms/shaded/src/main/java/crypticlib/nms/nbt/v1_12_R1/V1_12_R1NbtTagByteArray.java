package crypticlib.nms.nbt.v1_12_R1;

import crypticlib.nms.nbt.NbtTagByteArray;
import net.minecraft.server.v1_12_R1.NBTTagByteArray;

public class V1_12_R1NbtTagByteArray extends NbtTagByteArray {

    public V1_12_R1NbtTagByteArray(byte[] value) {
        super(value);
    }

    public V1_12_R1NbtTagByteArray(Object object) {
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
