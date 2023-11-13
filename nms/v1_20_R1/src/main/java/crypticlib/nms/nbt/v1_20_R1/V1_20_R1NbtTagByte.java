package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.NbtTagByte;
import net.minecraft.nbt.NBTTagByte;

public class V1_20_R1NbtTagByte extends NbtTagByte {

    public V1_20_R1NbtTagByte(byte value) {
        super(value);
    }

    public V1_20_R1NbtTagByte(Object nmsNbtByte) {
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
