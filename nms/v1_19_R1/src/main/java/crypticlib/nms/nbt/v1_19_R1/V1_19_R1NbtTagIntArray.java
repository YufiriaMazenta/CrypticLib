package crypticlib.nms.nbt.v1_19_R1;

import crypticlib.nms.nbt.NbtTagIntArray;
import net.minecraft.nbt.NBTTagIntArray;
import org.jetbrains.annotations.NotNull;

public class V1_19_R1NbtTagIntArray extends NbtTagIntArray {

    public V1_19_R1NbtTagIntArray(int[] value) {
        super(value);
    }

    public V1_19_R1NbtTagIntArray(Object nmsNbtIntArray) {
        super(nmsNbtIntArray);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagIntArray nbtTagIntArray = (NBTTagIntArray) nmsNbt;
        setValue(nbtTagIntArray.f());
    }

    @Override
    public @NotNull NBTTagIntArray toNms() {
        return new NBTTagIntArray(value);
    }
}
