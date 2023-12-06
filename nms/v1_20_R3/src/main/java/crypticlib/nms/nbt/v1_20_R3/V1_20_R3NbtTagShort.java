package crypticlib.nms.nbt.v1_20_R3;

import crypticlib.nms.nbt.NbtTagShort;
import net.minecraft.nbt.NBTTagShort;
import org.jetbrains.annotations.NotNull;

public class V1_20_R3NbtTagShort extends NbtTagShort {

    public V1_20_R3NbtTagShort(short value) {
        super(value);
    }

    public V1_20_R3NbtTagShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.h());
    }

    @Override
    public @NotNull NBTTagShort toNms() {
        return NBTTagShort.a(value());
    }
}
