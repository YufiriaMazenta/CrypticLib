package crypticlib.nms.nbt.v1_19_R2;

import crypticlib.nms.nbt.NbtTagFloat;
import net.minecraft.nbt.NBTTagFloat;

public class V1_19_R2NbtTagFloat extends NbtTagFloat {

    public V1_19_R2NbtTagFloat(float value) {
        super(value);
    }

    public V1_19_R2NbtTagFloat(Object nmsNbtFloat) {
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
