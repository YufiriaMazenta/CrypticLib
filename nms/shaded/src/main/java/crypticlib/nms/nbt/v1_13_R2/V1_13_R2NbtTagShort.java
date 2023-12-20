package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.NbtTagShort;
import net.minecraft.server.v1_13_R2.NBTTagShort;
import org.jetbrains.annotations.NotNull;

public class V1_13_R2NbtTagShort extends NbtTagShort {

    public V1_13_R2NbtTagShort(short value) {
        super(value);
    }

    public V1_13_R2NbtTagShort(Object nmsNbtShort) {
        super(nmsNbtShort);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagShort nbtTagShort = (NBTTagShort) nmsNbt;
        setValue(nbtTagShort.asShort());
    }

    @Override
    public @NotNull NBTTagShort toNms() {
        return new NBTTagShort(value);
    }
}
