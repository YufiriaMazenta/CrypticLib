package crypticlib.nms.nbt.v1_18_R2;

import crypticlib.nms.nbt.AbstractNbtFloat;
import net.minecraft.nbt.NBTTagFloat;

public class V1_18_R2NbtFloat extends AbstractNbtFloat {

    public V1_18_R2NbtFloat(float value) {
        super(value);
    }

    public V1_18_R2NbtFloat(Object nmsNbtFloat) {
        super(nmsNbtFloat);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagFloat nbtTagFloat = (NBTTagFloat) nmsNbt;
        setValue(nbtTagFloat.j());
    }

    @Override
    public NBTTagFloat toNms() {
        return NBTTagFloat.a(value());
    }

}
