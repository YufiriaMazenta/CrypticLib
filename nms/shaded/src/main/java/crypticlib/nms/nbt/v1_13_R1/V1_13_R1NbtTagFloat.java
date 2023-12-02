package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.NbtTagFloat;
import net.minecraft.server.v1_13_R1.NBTTagFloat;
import org.jetbrains.annotations.NotNull;

public class V1_13_R1NbtTagFloat extends NbtTagFloat {

    public V1_13_R1NbtTagFloat(float value) {
        super(value);
    }

    public V1_13_R1NbtTagFloat(Object nmsNbtFloat) {
        super(nmsNbtFloat);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagFloat nbtTagFloat = (NBTTagFloat) nmsNbt;
        setValue(nbtTagFloat.i());
    }

    @Override
    public @NotNull NBTTagFloat toNms() {
        return new NBTTagFloat(value());
    }

}
