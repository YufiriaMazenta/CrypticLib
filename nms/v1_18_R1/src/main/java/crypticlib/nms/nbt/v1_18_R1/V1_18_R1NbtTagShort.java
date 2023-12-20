package crypticlib.nms.nbt.v1_18_R1;

import crypticlib.nms.nbt.NbtTagShort;
import net.minecraft.nbt.NBTTagShort;
import org.jetbrains.annotations.NotNull;

public class V1_18_R1NbtTagShort extends NbtTagShort {

    public V1_18_R1NbtTagShort(short value) {
        super(value);
    }

    public V1_18_R1NbtTagShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.g());
    }

    @Override
    public @NotNull NBTTagShort toNms() {
        return NBTTagShort.a(value);
    }
}
