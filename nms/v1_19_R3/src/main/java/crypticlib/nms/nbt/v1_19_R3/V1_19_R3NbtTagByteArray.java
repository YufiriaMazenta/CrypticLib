package crypticlib.nms.nbt.v1_19_R3;

import crypticlib.nms.nbt.NbtTagByteArray;
import net.minecraft.nbt.NBTTagByteArray;

public class V1_19_R3NbtTagByteArray extends NbtTagByteArray {

    public V1_19_R3NbtTagByteArray(byte[] value) {
        super(value);
    }

    public V1_19_R3NbtTagByteArray(Object object) {
        super(object);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagByteArray nbtTagByteArray = (NBTTagByteArray) nmsNbt;
        setValue(nbtTagByteArray.e());
    }

    @Override
    public NBTTagByteArray toNms() {
        return new NBTTagByteArray(value());
    }

}
