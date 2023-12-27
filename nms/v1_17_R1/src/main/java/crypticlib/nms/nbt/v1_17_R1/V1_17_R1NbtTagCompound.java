package crypticlib.nms.nbt.v1_17_R1;

import crypticlib.nms.nbt.NbtTagCompound;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class V1_17_R1NbtTagCompound extends NbtTagCompound {

    public V1_17_R1NbtTagCompound() {
        super(V1_17_R1NbtTranslator.INSTANCE);
    }

    public V1_17_R1NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_17_R1NbtTranslator.INSTANCE);
    }

    public V1_17_R1NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_17_R1NbtTranslator.INSTANCE);
    }

    public V1_17_R1NbtTagCompound(String mojangson) {
        super(mojangson, V1_17_R1NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
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
        return new V1_17_R1NbtTagCompound(toString());
    }

}
