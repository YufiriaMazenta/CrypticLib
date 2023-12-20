package crypticlib.nms.nbt.v1_19_R1;

import crypticlib.nms.nbt.NbtTagDouble;
import net.minecraft.nbt.NBTTagDouble;
import org.jetbrains.annotations.NotNull;

public class V1_19_R1NbtTagDouble extends NbtTagDouble {

    public V1_19_R1NbtTagDouble(double value) {
        super(value);
    }

    public V1_19_R1NbtTagDouble(Object nmsNbtDouble) {
        super(nmsNbtDouble);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagDouble nbtTagDouble = (NBTTagDouble) nmsNbt;
        setValue(nbtTagDouble.i());
    }

    @Override
    public @NotNull NBTTagDouble toNms() {
        return NBTTagDouble.a(value);
    }

}
