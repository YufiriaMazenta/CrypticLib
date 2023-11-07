package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.AbstractNbtInt;
import net.minecraft.server.v1_13_R1.NBTTagInt;

public class V1_13_R1NbtInt extends AbstractNbtInt {

    public V1_13_R1NbtInt(int value) {
        super(value);
    }

    public V1_13_R1NbtInt(Object nmsNbtInt) {
        super(nmsNbtInt);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagInt nbtTagInt = (NBTTagInt) nmsNbt;
        setValue(nbtTagInt.e());
    }

    @Override
    public NBTTagInt toNms() {
        return new NBTTagInt(value());
    }

}
