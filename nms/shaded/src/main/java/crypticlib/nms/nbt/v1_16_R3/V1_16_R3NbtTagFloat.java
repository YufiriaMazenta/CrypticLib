package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.NbtTagFloat;
import net.minecraft.server.v1_16_R3.NBTTagFloat;

public class V1_16_R3NbtTagFloat extends NbtTagFloat {

    public V1_16_R3NbtTagFloat(float value) {
        super(value);
    }

    public V1_16_R3NbtTagFloat(Object nmsNbtFloat) {
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
