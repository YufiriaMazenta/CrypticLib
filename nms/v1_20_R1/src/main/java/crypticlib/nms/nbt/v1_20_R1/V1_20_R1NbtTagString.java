package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.NbtTagString;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import org.jetbrains.annotations.NotNull;

public class V1_20_R1NbtTagString extends NbtTagString {

    public V1_20_R1NbtTagString(String value) {
        super(value);
    }

    public V1_20_R1NbtTagString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.m_());
    }

    @Override
    public @NotNull NBTBase toNms() {
        return NBTTagString.a(value);
    }

}
