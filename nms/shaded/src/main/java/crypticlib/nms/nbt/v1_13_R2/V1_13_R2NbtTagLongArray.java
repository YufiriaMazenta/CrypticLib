package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.NbtTagLongArray;
import net.minecraft.server.v1_13_R2.NBTTagLongArray;
import org.jetbrains.annotations.NotNull;

public class V1_13_R2NbtTagLongArray extends NbtTagLongArray {

    public V1_13_R2NbtTagLongArray(long[] value) {
        super(value);
    }

    public V1_13_R2NbtTagLongArray(Object nmsNbtLongArray) {
        super(nmsNbtLongArray);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagLongArray nbtTagLongArray = (NBTTagLongArray) nmsNbt;
        setValue(nbtTagLongArray.d());
    }

    @Override
    public @NotNull NBTTagLongArray toNms() {
        return new NBTTagLongArray(value);
    }
}
