package crypticlib.nms.nbt.v1_19_R2;

import crypticlib.nms.nbt.AbstractNbtLong;
import net.minecraft.nbt.NBTTagLong;

public class V1_19_R2NbtLong extends AbstractNbtLong {

    public V1_19_R2NbtLong(long value) {
        super(value);
    }

    public V1_19_R2NbtLong(Object nmsNbtLong) {
        super(nmsNbtLong);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagLong nbtTagLong = (NBTTagLong) nmsNbt;
        setValue(nbtTagLong.f());
    }

    @Override
    public NBTTagLong toNms() {
        return NBTTagLong.a(value());
    }

}
