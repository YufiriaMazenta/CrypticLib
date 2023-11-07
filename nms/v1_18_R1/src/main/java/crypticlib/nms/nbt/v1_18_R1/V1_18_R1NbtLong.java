package crypticlib.nms.nbt.v1_18_R1;

import crypticlib.nms.nbt.AbstractNbtLong;
import net.minecraft.nbt.NBTTagLong;

public class V1_18_R1NbtLong extends AbstractNbtLong {

    public V1_18_R1NbtLong(long value) {
        super(value);
    }

    public V1_18_R1NbtLong(Object nmsNbtLong) {
        super(nmsNbtLong);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagLong nbtTagLong = (NBTTagLong) nmsNbt;
        setValue(nbtTagLong.e());
    }

    @Override
    public NBTTagLong toNms() {
        return NBTTagLong.a(value());
    }

}
