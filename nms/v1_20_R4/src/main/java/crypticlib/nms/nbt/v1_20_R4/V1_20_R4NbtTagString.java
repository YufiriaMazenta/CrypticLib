package crypticlib.nms.nbt.v1_20_R4;

import crypticlib.nms.nbt.NbtTagString;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import org.jetbrains.annotations.NotNull;

public class V1_20_R4NbtTagString extends NbtTagString {

    public V1_20_R4NbtTagString(String value) {
        super(value);
    }

    public V1_20_R4NbtTagString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.s_());
    }

    @Override
    public @NotNull NBTBase toNms() {
        return NBTTagString.a(value);
    }

}
