package crypticlib.nms.nbt.v1_15_R1;

import crypticlib.nms.nbt.AbstractNbtByte;
import net.minecraft.server.v1_15_R1.NBTTagByte;

public class V1_15_R1NbtByte extends AbstractNbtByte {

    public V1_15_R1NbtByte(byte value) {
        super(value);
    }

    public V1_15_R1NbtByte(Object nmsNbtByte) {
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
