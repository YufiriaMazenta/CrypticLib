package crypticlib.nms.nbt.v1_15_R1;

import crypticlib.nms.nbt.NbtTagFloat;
import net.minecraft.server.v1_15_R1.NBTTagFloat;

public class V1_15_R1NbtTagFloat extends NbtTagFloat {

    public V1_15_R1NbtTagFloat(float value) {
        super(value);
    }

    public V1_15_R1NbtTagFloat(Object nmsNbtFloat) {
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
