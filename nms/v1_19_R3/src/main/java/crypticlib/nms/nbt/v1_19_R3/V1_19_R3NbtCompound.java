package crypticlib.nms.nbt.v1_19_R3;

import crypticlib.nms.nbt.AbstractNbtCompound;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class V1_19_R3NbtCompound extends AbstractNbtCompound {

    public V1_19_R3NbtCompound() {
        super(V1_19_R3NbtTranslator.INSTANCE);
    }

    public V1_19_R3NbtCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_19_R3NbtTranslator.INSTANCE);
    }

    public V1_19_R3NbtCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_19_R3NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        for (String key : nms.e()) {
            value().put(key, getNbtTranslator().fromNms(nms.c(key)));
        }
    }

    @Override
    public NBTTagCompound toNms() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        for (String key : value().keySet()) {
            nbtTagCompound.a(key, (NBTBase) get(key).toNms());
        }
        return nbtTagCompound;
    }

}
