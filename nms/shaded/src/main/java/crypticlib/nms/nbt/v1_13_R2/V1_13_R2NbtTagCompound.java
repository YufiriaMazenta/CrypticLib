package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.NbtTagCompound;
import net.minecraft.server.v1_13_R2.MojangsonParser;
import net.minecraft.server.v1_13_R2.NBTBase;
import net.minecraft.server.v1_13_R2.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class V1_13_R2NbtTagCompound extends NbtTagCompound {

    public V1_13_R2NbtTagCompound() {
        super(V1_13_R2NbtTranslator.INSTANCE);
    }

    public V1_13_R2NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_13_R2NbtTranslator.INSTANCE);
    }

    public V1_13_R2NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_13_R2NbtTranslator.INSTANCE);
    }

    public V1_13_R2NbtTagCompound(String mojangson) {
        super(mojangson, V1_13_R2NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        nbtMap.clear();
        for (String key : nms.getKeys()) {
            nbtMap.put(key, nbtTranslator.translateNmsNbt(nms.get(key)));
        }
    }

    @Override
    public @NotNull NBTTagCompound toNms() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        for (String key : nbtMap.keySet()) {
            nbtTagCompound.set(key, (NBTBase) get(key).toNms());
        }
        return nbtTagCompound;
    }

    @Override
    public void fromMojangson(String mojangson) {
        try {
            fromNms(MojangsonParser.parse(mojangson));
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NbtTagCompound clone() {
        return new V1_13_R2NbtTagCompound(toString());
    }

}
