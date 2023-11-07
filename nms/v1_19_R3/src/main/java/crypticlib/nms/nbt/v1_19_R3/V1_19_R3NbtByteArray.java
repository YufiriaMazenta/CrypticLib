package crypticlib.nms.nbt.v1_19_R3;

import crypticlib.nms.nbt.AbstractNbtByteArray;
import net.minecraft.nbt.NBTTagByteArray;

public class V1_19_R3NbtByteArray extends AbstractNbtByteArray {

    public V1_19_R3NbtByteArray(byte[] value) {
        super(value);
    }

    public V1_19_R3NbtByteArray(Object object) {
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
