package crypticlib.nms.nbt.v1_19_R2;

import crypticlib.nms.nbt.NbtTagString;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagString;
import org.jetbrains.annotations.NotNull;

public class V1_19_R2NbtTagString extends NbtTagString {

    public V1_19_R2NbtTagString(String value) {
        super(value);
    }

    public V1_19_R2NbtTagString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.f_());
    }

    @Override
    public @NotNull NBTBase toNms() {
        return NBTTagString.a(value());
    }

}
