package crypticlib.nms.nbt.v1_14_R1;

import crypticlib.nms.nbt.NbtTagByteArray;
import net.minecraft.server.v1_14_R1.NBTTagByteArray;
import org.jetbrains.annotations.NotNull;

public class V1_14_R1NbtTagByteArray extends NbtTagByteArray {

    public V1_14_R1NbtTagByteArray(byte[] value) {
        super(value);
    }

    public V1_14_R1NbtTagByteArray(Object object) {
        super(object);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagByteArray nbtTagByteArray = (NBTTagByteArray) nmsNbt;
        setValue(nbtTagByteArray.getBytes());
    }

    @Override
    public @NotNull NBTTagByteArray toNms() {
        return new NBTTagByteArray(value());
    }

}
