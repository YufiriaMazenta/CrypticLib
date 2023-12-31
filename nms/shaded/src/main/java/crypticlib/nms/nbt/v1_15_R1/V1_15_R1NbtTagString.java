package crypticlib.nms.nbt.v1_15_R1;

import crypticlib.nms.nbt.NbtTagString;
import net.minecraft.server.v1_15_R1.NBTBase;
import net.minecraft.server.v1_15_R1.NBTTagString;
import org.jetbrains.annotations.NotNull;

public class V1_15_R1NbtTagString extends NbtTagString {

    public V1_15_R1NbtTagString(String value) {
        super(value);
    }

    public V1_15_R1NbtTagString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.asString());
    }

    @Override
    public @NotNull NBTBase toNms() {
        return NBTTagString.a(value);
    }

}
