package crypticlib.nms.nbt.v1_20_R2;

import crypticlib.nms.nbt.NbtTagByteArray;
import net.minecraft.nbt.NBTTagByteArray;
import org.jetbrains.annotations.NotNull;

public class V1_20_R2NbtTagByteArray extends NbtTagByteArray {

    public V1_20_R2NbtTagByteArray(byte[] value) {
        super(value);
    }

    public V1_20_R2NbtTagByteArray(Object object) {
        super(object);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagByteArray nbtTagByteArray = (NBTTagByteArray) nmsNbt;
        setValue(nbtTagByteArray.e());
    }

    @Override
    public @NotNull NBTTagByteArray toNms() {
        return new NBTTagByteArray(value());
    }

}
