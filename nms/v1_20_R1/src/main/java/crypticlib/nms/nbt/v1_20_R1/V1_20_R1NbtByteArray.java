package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.AbstractNbtByteArray;
import net.minecraft.nbt.NBTTagByteArray;

public class V1_20_R1NbtByteArray extends AbstractNbtByteArray {

    public V1_20_R1NbtByteArray(byte[] value) {
        super(value);
    }

    public V1_20_R1NbtByteArray(Object object) {
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
