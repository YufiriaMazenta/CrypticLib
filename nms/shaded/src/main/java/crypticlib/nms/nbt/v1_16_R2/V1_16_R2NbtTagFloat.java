package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.NbtTagFloat;
import net.minecraft.server.v1_16_R2.NBTTagFloat;
import org.jetbrains.annotations.NotNull;

public class V1_16_R2NbtTagFloat extends NbtTagFloat {

    public V1_16_R2NbtTagFloat(float value) {
        super(value);
    }

    public V1_16_R2NbtTagFloat(Object nmsNbtFloat) {
        super(nmsNbtFloat);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagFloat nbtTagFloat = (NBTTagFloat) nmsNbt;
        setValue(nbtTagFloat.asFloat());
    }

    @Override
    public @NotNull NBTTagFloat toNms() {
        return NBTTagFloat.a(value());
    }

}
