package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.NbtTagCompound;
import net.minecraft.server.v1_16_R2.NBTBase;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class V1_16_R2NbtTagCompound extends NbtTagCompound {

    public V1_16_R2NbtTagCompound() {
        super(V1_16_R2NbtTranslator.INSTANCE);
    }

    public V1_16_R2NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_16_R2NbtTranslator.INSTANCE);
    }

    public V1_16_R2NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_16_R2NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        for (String key : nms.getKeys()) {
            value().put(key, nbtTranslator().translateNmsNbt(nms.get(key)));
        }
    }

    @Override
    public @NotNull NBTTagCompound toNms() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        for (String key : value().keySet()) {
            nbtTagCompound.set(key, (NBTBase) get(key).toNms());
        }
        return nbtTagCompound;
    }

}
