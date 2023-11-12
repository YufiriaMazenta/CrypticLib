package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.AbstractNbtString;
import net.minecraft.server.v1_13_R1.NBTBase;
import net.minecraft.server.v1_13_R1.NBTTagString;

public class V1_13_R1NbtString extends AbstractNbtString {

    public V1_13_R1NbtString(String value) {
        super(value);
    }

    public V1_13_R1NbtString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.b_());
    }

    @Override
    public NBTBase toNms() {
        return new NBTTagString(value());
    }

}
