package crypticlib.nms.nbt.v1_16_R1;

import crypticlib.nms.nbt.AbstractNbtTagCompound;
import net.minecraft.server.v1_16_R1.NBTBase;
import net.minecraft.server.v1_16_R1.NBTTagCompound;

import java.util.Map;

public class V1_16_R1NbtTagCompound extends AbstractNbtTagCompound {

    public V1_16_R1NbtTagCompound() {
        super(V1_16_R1NbtTranslator.INSTANCE);
    }

    public V1_16_R1NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_16_R1NbtTranslator.INSTANCE);
    }

    public V1_16_R1NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_16_R1NbtTranslator.INSTANCE);
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
