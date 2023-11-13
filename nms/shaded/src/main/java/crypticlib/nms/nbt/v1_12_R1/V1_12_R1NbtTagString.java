package crypticlib.nms.nbt.v1_12_R1;

import crypticlib.nms.nbt.NbtTagString;
import net.minecraft.server.v1_13_R1.NBTBase;
import net.minecraft.server.v1_13_R1.NBTTagString;

public class V1_12_R1NbtTagString extends NbtTagString {

    public V1_12_R1NbtTagString(String value) {
        super(value);
    }

    public V1_12_R1NbtTagString(Object nmsNbtString) {
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
