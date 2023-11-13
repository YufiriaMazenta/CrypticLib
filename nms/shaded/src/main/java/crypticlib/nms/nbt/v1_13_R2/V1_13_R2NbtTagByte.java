package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.NbtTagByte;
import net.minecraft.server.v1_13_R2.NBTTagByte;

public class V1_13_R2NbtTagByte extends NbtTagByte {

    public V1_13_R2NbtTagByte(byte value) {
        super(value);
    }

    public V1_13_R2NbtTagByte(Object nmsNbtByte) {
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
