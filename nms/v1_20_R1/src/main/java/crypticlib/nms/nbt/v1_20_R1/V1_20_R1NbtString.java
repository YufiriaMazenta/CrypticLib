package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.AbstractNbtString;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

public class V1_20_R1NbtString extends AbstractNbtString {

    public V1_20_R1NbtString(String value) {
        super(value);
    }

    public V1_20_R1NbtString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.m_());
    }

    @Override
    public NBTBase toNms() {
        return NBTTagString.a(value());
    }

}
