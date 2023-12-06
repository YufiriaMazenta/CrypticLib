package crypticlib.nms.nbt.v1_20_R3;

import crypticlib.nms.nbt.NbtTagByte;
import net.minecraft.nbt.NBTTagByte;
import org.jetbrains.annotations.NotNull;

public class V1_20_R3NbtTagByte extends NbtTagByte {

    public V1_20_R3NbtTagByte(byte value) {
        super(value);
    }

    public V1_20_R3NbtTagByte(Object nmsNbtByte) {
        super(nmsNbtByte);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagByte nbtTagByte = (NBTTagByte) nmsNbt;
        setValue(nbtTagByte.i());
    }

    @Override
    public @NotNull NBTTagByte toNms() {
        return NBTTagByte.a(value());
    }

}
