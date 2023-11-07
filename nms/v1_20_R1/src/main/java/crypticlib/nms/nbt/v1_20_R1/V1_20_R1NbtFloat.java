package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.AbstractNbtFloat;
import net.minecraft.nbt.NBTTagFloat;

public class V1_20_R1NbtFloat extends AbstractNbtFloat {

    public V1_20_R1NbtFloat(float value) {
        super(value);
    }

    public V1_20_R1NbtFloat(Object nmsNbtFloat) {
        super(nmsNbtFloat);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagFloat nbtTagFloat = (NBTTagFloat) nmsNbt;
        setValue(nbtTagFloat.k());
    }

    @Override
    public NBTTagFloat toNms() {
        return NBTTagFloat.a(value());
    }

}
