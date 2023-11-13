package crypticlib.nms.nbt.v1_12_R1;

import crypticlib.nms.nbt.NbtTagLong;
import net.minecraft.server.v1_12_R1.NBTTagLong;

public class V1_12_R1NbtTagLong extends NbtTagLong {

    public V1_12_R1NbtTagLong(long value) {
        super(value);
    }

    public V1_12_R1NbtTagLong(Object nmsNbtLong) {
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
