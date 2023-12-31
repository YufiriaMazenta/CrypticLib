package crypticlib.nms.nbt.v1_12_R1;

import crypticlib.nms.nbt.NbtTagByte;
import net.minecraft.server.v1_12_R1.NBTTagByte;
import org.jetbrains.annotations.NotNull;

public class V1_12_R1NbtTagByte extends NbtTagByte {

    public V1_12_R1NbtTagByte(byte value) {
        super(value);
    }

    public V1_12_R1NbtTagByte(Object nmsNbtByte) {
        super(nmsNbtByte);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagByte nbtTagByte = (NBTTagByte) nmsNbt;
        setValue(nbtTagByte.g());
    }

    @Override
    public @NotNull NBTTagByte toNms() {
        return new NBTTagByte(value);
    }

}
