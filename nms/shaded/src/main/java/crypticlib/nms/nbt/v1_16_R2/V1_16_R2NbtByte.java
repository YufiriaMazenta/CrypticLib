package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.AbstractNbtByte;
import net.minecraft.server.v1_16_R2.NBTTagByte;

public class V1_16_R2NbtByte extends AbstractNbtByte {

    public V1_16_R2NbtByte(byte value) {
        super(value);
    }

    public V1_16_R2NbtByte(Object nmsNbtByte) {
        super(nmsNbtByte);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagByte nbtTagByte = (NBTTagByte) nmsNbt;
        setValue(nbtTagByte.asByte());
    }

    @Override
    public NBTTagByte toNms() {
        return NBTTagByte.a(value());
    }

}
