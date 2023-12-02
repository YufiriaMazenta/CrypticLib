package crypticlib.nms.nbt.v1_18_R2;

import crypticlib.nms.nbt.NbtTagCompound;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class V1_18_R2NbtTagCompound extends NbtTagCompound {

    public V1_18_R2NbtTagCompound() {
        super(V1_18_R2NbtTranslator.INSTANCE);
    }

    public V1_18_R2NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_18_R2NbtTranslator.INSTANCE);
    }

    public V1_18_R2NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_18_R2NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        for (String key : nms.d()) {
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
