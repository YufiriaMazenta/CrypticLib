package crypticlib.nms.nbt.v1_20_R3;

import crypticlib.nms.nbt.NbtTagDouble;
import net.minecraft.nbt.NBTTagDouble;
import org.jetbrains.annotations.NotNull;

public class V1_20_R3NbtTagDouble extends NbtTagDouble {

    public V1_20_R3NbtTagDouble(double value) {
        super(value);
    }

    public V1_20_R3NbtTagDouble(Object nmsNbtDouble) {
        super(nmsNbtDouble);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagDouble nbtTagDouble = (NBTTagDouble) nmsNbt;
        setValue(nbtTagDouble.j());
    }

    @Override
    public @NotNull NBTTagDouble toNms() {
        return NBTTagDouble.a(value);
    }

}
