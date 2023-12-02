package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.NbtTagByteArray;
import net.minecraft.server.v1_13_R2.NBTTagByteArray;
import org.jetbrains.annotations.NotNull;

public class V1_13_R2NbtTagByteArray extends NbtTagByteArray {

    public V1_13_R2NbtTagByteArray(byte[] value) {
        super(value);
    }

    public V1_13_R2NbtTagByteArray(Object object) {
        super(object);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagByteArray nbtTagByteArray = (NBTTagByteArray) nmsNbt;
        setValue(nbtTagByteArray.c());
    }

    @Override
    public @NotNull NBTTagByteArray toNms() {
        return new NBTTagByteArray(value());
    }

}
