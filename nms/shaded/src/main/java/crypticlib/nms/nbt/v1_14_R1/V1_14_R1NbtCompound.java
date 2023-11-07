package crypticlib.nms.nbt.v1_14_R1;

import crypticlib.nms.nbt.AbstractNbtCompound;
import net.minecraft.server.v1_14_R1.NBTBase;
import net.minecraft.server.v1_14_R1.NBTTagCompound;

import java.util.Map;

public class V1_14_R1NbtCompound extends AbstractNbtCompound {

    public V1_14_R1NbtCompound() {
        super(V1_14_R1NbtTranslator.INSTANCE);
    }

    public V1_14_R1NbtCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_14_R1NbtTranslator.INSTANCE);
    }

    public V1_14_R1NbtCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_14_R1NbtTranslator.INSTANCE);
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
