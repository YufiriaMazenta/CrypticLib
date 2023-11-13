package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.NbtTagCompound;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.NBTTagCompound;

import java.util.Map;

public class V1_16_R3NbtTagCompound extends NbtTagCompound {

    public V1_16_R3NbtTagCompound() {
        super(V1_16_R3NbtTranslator.INSTANCE);
    }

    public V1_16_R3NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_16_R3NbtTranslator.INSTANCE);
    }

    public V1_16_R3NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_16_R3NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        for (String key : nms.getKeys()) {
            value().put(key, nbtTranslator().translateNmsNbt(nms.get(key)));
        }
    }

    @Override
    public NBTTagCompound toNms() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        for (String key : value().keySet()) {
            nbtTagCompound.set(key, (NBTBase) get(key).toNms());
        }
        return nbtTagCompound;
    }

}
