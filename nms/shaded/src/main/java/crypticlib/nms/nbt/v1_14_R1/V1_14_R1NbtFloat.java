package crypticlib.nms.nbt.v1_14_R1;

import crypticlib.nms.nbt.AbstractNbtFloat;
import net.minecraft.server.v1_14_R1.NBTTagFloat;

public class V1_14_R1NbtFloat extends AbstractNbtFloat {

    public V1_14_R1NbtFloat(float value) {
        super(value);
    }

    public V1_14_R1NbtFloat(Object nmsNbtFloat) {
        super(nmsNbtFloat);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagFloat nbtTagFloat = (NBTTagFloat) nmsNbt;
        setValue(nbtTagFloat.asFloat());
    }

    @Override
    public NBTTagFloat toNms() {
        return new NBTTagFloat(value());
    }

}
