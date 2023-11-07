package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.AbstractNbtByte;
import net.minecraft.server.v1_13_R1.NBTTagByte;

public class V1_13_R1NbtByte extends AbstractNbtByte {

    public V1_13_R1NbtByte(byte value) {
        super(value);
    }

    public V1_13_R1NbtByte(Object nmsNbtByte) {
        super(nmsNbtByte);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagByte nbtTagByte = (NBTTagByte) nmsNbt;
        setValue(nbtTagByte.g());
    }

    @Override
    public NBTTagByte toNms() {
        return new NBTTagByte(value());
    }

}
