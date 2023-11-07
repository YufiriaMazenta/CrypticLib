package crypticlib.nms.nbt.v1_19_R2;

import crypticlib.nms.nbt.AbstractNbtByte;
import net.minecraft.nbt.NBTTagByte;

public class V1_19_R2NbtByte extends AbstractNbtByte {

    public V1_19_R2NbtByte(byte value) {
        super(value);
    }

    public V1_19_R2NbtByte(Object nmsNbtByte) {
        super(nmsNbtByte);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagByte nbtTagByte = (NBTTagByte) nmsNbt;
        setValue(nbtTagByte.i());
    }

    @Override
    public NBTTagByte toNms() {
        return NBTTagByte.a(value());
    }

}
