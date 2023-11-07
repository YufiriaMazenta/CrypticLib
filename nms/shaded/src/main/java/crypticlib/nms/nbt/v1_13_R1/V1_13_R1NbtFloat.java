package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.AbstractNbtFloat;
import net.minecraft.server.v1_13_R1.NBTTagFloat;

public class V1_13_R1NbtFloat extends AbstractNbtFloat {

    public V1_13_R1NbtFloat(float value) {
        super(value);
    }

    public V1_13_R1NbtFloat(Object nmsNbtFloat) {
        super(nmsNbtFloat);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagFloat nbtTagFloat = (NBTTagFloat) nmsNbt;
        setValue(nbtTagFloat.i());
    }

    @Override
    public NBTTagFloat toNms() {
        return new NBTTagFloat(value());
    }

}
