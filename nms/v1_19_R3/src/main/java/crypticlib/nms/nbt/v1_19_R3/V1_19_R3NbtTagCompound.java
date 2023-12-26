package crypticlib.nms.nbt.v1_19_R3;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import crypticlib.nms.nbt.NbtTagCompound;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class V1_19_R3NbtTagCompound extends NbtTagCompound {

    public V1_19_R3NbtTagCompound() {
        super(V1_19_R3NbtTranslator.INSTANCE);
    }

    public V1_19_R3NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_19_R3NbtTranslator.INSTANCE);
    }

    public V1_19_R3NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_19_R3NbtTranslator.INSTANCE);
    }

    public V1_19_R3NbtTagCompound(String mojangson) {
        super(mojangson, V1_19_R3NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagCompound nms = (NBTTagCompound) nmsNbt;
        for (String key : nms.e()) {
            nbtMap.put(key, nbtTranslator.translateNmsNbt(nms.c(key)));
        }
    }

    @Override
    public @NotNull NBTTagCompound toNms() {
        NBTTagCompound nbtTagCompound = new NBTTagCompound();
        for (String key : nbtMap.keySet()) {
            nbtTagCompound.a(key, (NBTBase) get(key).toNms());
        }
        return nbtTagCompound;
    }

    @Override
    public void fromMojangson(String mojangson) {
        try {
            fromNms(MojangsonParser.a(mojangson));
        } catch (CommandSyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public NbtTagCompound clone() {
        return new V1_19_R3NbtTagCompound(toString());
    }

}
