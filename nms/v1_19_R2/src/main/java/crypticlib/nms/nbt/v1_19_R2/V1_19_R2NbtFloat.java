package crypticlib.nms.nbt.v1_19_R2;

import crypticlib.nms.nbt.AbstractNbtFloat;
import net.minecraft.nbt.NBTTagFloat;

public class V1_19_R2NbtFloat extends AbstractNbtFloat {

    public V1_19_R2NbtFloat(float value) {
        super(value);
    }

    public V1_19_R2NbtFloat(Object nmsNbtFloat) {
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
