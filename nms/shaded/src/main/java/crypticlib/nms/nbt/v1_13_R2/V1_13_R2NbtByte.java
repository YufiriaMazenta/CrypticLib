package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.AbstractNbtByte;
import net.minecraft.server.v1_13_R2.NBTTagByte;

public class V1_13_R2NbtByte extends AbstractNbtByte {

    public V1_13_R2NbtByte(byte value) {
        super(value);
    }

    public V1_13_R2NbtByte(Object nmsNbtByte) {
        super(nmsNbtByte);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagByte nbtTagByte = (NBTTagByte) nmsNbt;
        setValue(nbtTagByte.asByte());
    }

    @Override
    public NBTTagByte toNms() {
        return new NBTTagByte(value());
    }

}
