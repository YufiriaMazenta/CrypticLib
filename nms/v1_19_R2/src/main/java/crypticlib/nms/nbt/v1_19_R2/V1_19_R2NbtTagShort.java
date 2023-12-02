package crypticlib.nms.nbt.v1_19_R2;

import crypticlib.nms.nbt.NbtTagShort;
import net.minecraft.nbt.NBTTagShort;
import org.jetbrains.annotations.NotNull;

public class V1_19_R2NbtTagShort extends NbtTagShort {

    public V1_19_R2NbtTagShort(short value) {
        super(value);
    }

    public V1_19_R2NbtTagShort(Object nmsNbtShort) {
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
