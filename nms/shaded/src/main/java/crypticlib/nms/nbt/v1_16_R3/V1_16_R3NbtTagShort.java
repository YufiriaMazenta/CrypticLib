package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.NbtTagShort;
import net.minecraft.server.v1_16_R3.NBTTagShort;
import org.jetbrains.annotations.NotNull;

public class V1_16_R3NbtTagShort extends NbtTagShort {

    public V1_16_R3NbtTagShort(short value) {
        super(value);
    }

    public V1_16_R3NbtTagShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.asShort());
    }

    @Override
    public @NotNull NBTTagShort toNms() {
        return NBTTagShort.a(value());
    }
}
