package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.NbtTagInt;
import net.minecraft.server.v1_13_R2.NBTTagInt;
import org.jetbrains.annotations.NotNull;

public class V1_13_R2NbtTagInt extends NbtTagInt {

    public V1_13_R2NbtTagInt(int value) {
        super(value);
    }

    public V1_13_R2NbtTagInt(Object nmsNbtInt) {
        super(nmsNbtInt);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagInt nbtTagInt = (NBTTagInt) nmsNbt;
        setValue(nbtTagInt.asInt());
    }

    @Override
    public @NotNull NBTTagInt toNms() {
        return new NBTTagInt(value());
    }

}
