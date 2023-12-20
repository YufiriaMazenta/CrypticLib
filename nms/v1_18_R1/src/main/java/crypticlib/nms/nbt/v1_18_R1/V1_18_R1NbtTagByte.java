package crypticlib.nms.nbt.v1_18_R1;

import crypticlib.nms.nbt.NbtTagByte;
import net.minecraft.nbt.NBTTagByte;
import org.jetbrains.annotations.NotNull;

public class V1_18_R1NbtTagByte extends NbtTagByte {

    public V1_18_R1NbtTagByte(byte value) {
        super(value);
    }

    public V1_18_R1NbtTagByte(Object nmsNbtByte) {
        super(nmsNbtByte);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagByte nbtTagByte = (NBTTagByte) nmsNbt;
        setValue(nbtTagByte.h());
    }

    @Override
    public @NotNull NBTTagByte toNms() {
        return NBTTagByte.a(value);
    }

}
