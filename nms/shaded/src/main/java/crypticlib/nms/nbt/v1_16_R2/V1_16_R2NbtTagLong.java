package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.NbtTagLong;
import net.minecraft.server.v1_16_R2.NBTTagLong;
import org.jetbrains.annotations.NotNull;

public class V1_16_R2NbtTagLong extends NbtTagLong {

    public V1_16_R2NbtTagLong(long value) {
        super(value);
    }

    public V1_16_R2NbtTagLong(Object nmsNbtLong) {
        super(nmsNbtLong);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagLong nbtTagLong = (NBTTagLong) nmsNbt;
        setValue(nbtTagLong.asLong());
    }

    @Override
    public @NotNull NBTTagLong toNms() {
        return NBTTagLong.a(value());
    }

}
