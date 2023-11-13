package crypticlib.nms.nbt.v1_19_R3;

import crypticlib.nms.nbt.NbtTagString;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;

public class V1_19_R3NbtTagString extends NbtTagString {

    public V1_19_R3NbtTagString(String value) {
        super(value);
    }

    public V1_19_R3NbtTagString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.f_());
    }

    @Override
    public NBTBase toNms() {
        return NBTTagString.a(value());
    }

}
