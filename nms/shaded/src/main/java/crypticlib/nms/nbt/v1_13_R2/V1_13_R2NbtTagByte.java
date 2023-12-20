package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.NbtTagByte;
import net.minecraft.server.v1_13_R2.NBTTagByte;
import org.jetbrains.annotations.NotNull;

public class V1_13_R2NbtTagByte extends NbtTagByte {

    public V1_13_R2NbtTagByte(byte value) {
        super(value);
    }

    public V1_13_R2NbtTagByte(Object nmsNbtByte) {
        super(nmsNbtByte);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagByte nbtTagByte = (NBTTagByte) nmsNbt;
        setValue(nbtTagByte.asByte());
    }

    @Override
    public @NotNull NBTTagByte toNms() {
        return new NBTTagByte(value);
    }

}
