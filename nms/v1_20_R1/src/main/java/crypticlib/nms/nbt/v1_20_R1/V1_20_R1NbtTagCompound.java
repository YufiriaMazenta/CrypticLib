package crypticlib.nms.nbt.v1_20_R1;

import crypticlib.nms.nbt.AbstractNbtTagCompound;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class V1_20_R1NbtTagCompound extends AbstractNbtTagCompound {

    public V1_20_R1NbtTagCompound() {
        super(V1_20_R1NbtTranslator.INSTANCE);
    }

    public V1_20_R1NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_20_R1NbtTranslator.INSTANCE);
    }

    public V1_20_R1NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_20_R1NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        for (String key : nms.e()) {
            value().put(key, nbtTranslator().translateNmsNbt(nms.c(key)));
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
