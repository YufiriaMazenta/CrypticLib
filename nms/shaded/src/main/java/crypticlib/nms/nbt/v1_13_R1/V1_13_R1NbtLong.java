package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.AbstractNbtLong;
import net.minecraft.server.v1_13_R1.NBTTagLong;

public class V1_13_R1NbtLong extends AbstractNbtLong {

    public V1_13_R1NbtLong(long value) {
        super(value);
    }

    public V1_13_R1NbtLong(Object nmsNbtLong) {
        super(nmsNbtLong);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagLong nbtTagLong = (NBTTagLong) nmsNbt;
        setValue(nbtTagLong.d());
    }

    @Override
    public NBTTagLong toNms() {
        return new NBTTagLong(value());
    }

}
