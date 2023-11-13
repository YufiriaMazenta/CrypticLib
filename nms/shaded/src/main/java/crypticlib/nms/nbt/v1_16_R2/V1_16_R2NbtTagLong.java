package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.NbtTagLong;
import net.minecraft.server.v1_16_R2.NBTTagLong;

public class V1_16_R2NbtTagLong extends NbtTagLong {

    public V1_16_R2NbtTagLong(long value) {
        super(value);
    }

    public V1_16_R2NbtTagLong(Object nmsNbtLong) {
        super(nmsNbtLong);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagLong nbtTagLong = (NBTTagLong) nmsNbt;
        setValue(nbtTagLong.asLong());
    }

    @Override
    public NBTTagLong toNms() {
        return NBTTagLong.a(value());
    }

}
