package crypticlib.nms.nbt.v1_16_R1;

import crypticlib.nms.nbt.NbtTagLongArray;
import net.minecraft.server.v1_16_R1.NBTTagLongArray;
import org.jetbrains.annotations.NotNull;

public class V1_16_R1NbtTagLongArray extends NbtTagLongArray {

    public V1_16_R1NbtTagLongArray(long[] value) {
        super(value);
    }

    public V1_16_R1NbtTagLongArray(Object nmsNbtLongArray) {
        super(nmsNbtLongArray);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagLongArray nbtTagLongArray = (NBTTagLongArray) nmsNbt;
        setValue(nbtTagLongArray.getLongs());
    }

    @Override
    public @NotNull NBTTagLongArray toNms() {
        return new NBTTagLongArray(value);
    }
}
