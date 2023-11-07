package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.AbstractNbtFloat;
import net.minecraft.server.v1_16_R2.NBTTagFloat;

public class V1_16_R2NbtFloat extends AbstractNbtFloat {

    public V1_16_R2NbtFloat(float value) {
        super(value);
    }

    public V1_16_R2NbtFloat(Object nmsNbtFloat) {
        super(nmsNbtFloat);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagFloat nbtTagFloat = (NBTTagFloat) nmsNbt;
        setValue(nbtTagFloat.asFloat());
    }

    @Override
    public NBTTagFloat toNms() {
        return NBTTagFloat.a(value());
    }

}
