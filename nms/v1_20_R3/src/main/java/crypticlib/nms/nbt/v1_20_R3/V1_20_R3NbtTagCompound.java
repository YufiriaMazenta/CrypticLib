package crypticlib.nms.nbt.v1_20_R3;

import crypticlib.nms.nbt.NbtTagCompound;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class V1_20_R3NbtTagCompound extends NbtTagCompound {

    public V1_20_R3NbtTagCompound() {
        super(V1_20_R3NbtTranslator.INSTANCE);
    }

    public V1_20_R3NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_20_R3NbtTranslator.INSTANCE);
    }

    public V1_20_R3NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_20_R3NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        for (String key : nms.e()) {
            value().put(key, nbtTranslator().translateNmsNbt(nms.c(key)));
        }
    }

    @Override
    public @NotNull NBTTagCompound toNms() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        for (String key : value().keySet()) {
            nbtTagCompound.a(key, (NBTBase) get(key).toNms());
        }
        return nbtTagCompound;
    }

}