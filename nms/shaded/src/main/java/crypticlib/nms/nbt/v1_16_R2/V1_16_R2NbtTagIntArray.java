package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.NbtTagIntArray;
import net.minecraft.server.v1_16_R2.NBTTagIntArray;
import org.jetbrains.annotations.NotNull;

public class V1_16_R2NbtTagIntArray extends NbtTagIntArray {

    public V1_16_R2NbtTagIntArray(int[] value) {
        super(value);
    }

    public V1_16_R2NbtTagIntArray(Object nmsNbtIntArray) {
        super(nmsNbtIntArray);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagIntArray nbtTagIntArray = (NBTTagIntArray) nmsNbt;
        setValue(nbtTagIntArray.getInts());
    }

    @Override
    public @NotNull NBTTagIntArray toNms() {
        return new NBTTagIntArray(value());
    }

}
