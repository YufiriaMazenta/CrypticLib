package crypticlib.nms.nbt.v1_16_R2;

import crypticlib.nms.nbt.AbstractNbtCompound;
import net.minecraft.server.v1_16_R2.NBTBase;
import net.minecraft.server.v1_16_R2.NBTTagCompound;

import java.util.Map;

public class V1_16_R2NbtCompound extends AbstractNbtCompound {

    public V1_16_R2NbtCompound() {
        super(V1_16_R2NbtTranslator.INSTANCE);
    }

    public V1_16_R2NbtCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_16_R2NbtTranslator.INSTANCE);
    }

    public V1_16_R2NbtCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_16_R2NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        for (String key : nms.getKeys()) {
            value().put(key, getNbtTranslator().fromNms(nms.get(key)));
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
