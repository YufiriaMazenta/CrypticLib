package crypticlib.nms.nbt.v1_20_R2;

import crypticlib.nms.nbt.AbstractNbtString;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

public class V1_20_R2NbtString extends AbstractNbtString {

    public V1_20_R2NbtString(String value) {
        super(value);
    }

    public V1_20_R2NbtString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.r_());
    }

    @Override
    public NBTBase toNms() {
        return NBTTagString.a(value());
    }

}
