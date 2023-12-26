package crypticlib.nms.nbt.v1_20_R2;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import crypticlib.nms.nbt.NbtTagCompound;
import net.minecraft.nbt.MojangsonParser;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class V1_20_R2NbtTagCompound extends NbtTagCompound {

    public V1_20_R2NbtTagCompound() {
        super(V1_20_R2NbtTranslator.INSTANCE);
    }

    public V1_20_R2NbtTagCompound(Object nmsNbtCompound) {
        super(nmsNbtCompound, V1_20_R2NbtTranslator.INSTANCE);
    }

    public V1_20_R2NbtTagCompound(Map<String, Object> nbtValueMap) {
        super(nbtValueMap, V1_20_R2NbtTranslator.INSTANCE);
    }

    public V1_20_R2NbtTagCompound(String mojangson) {
        super(mojangson, V1_20_R2NbtTranslator.INSTANCE);
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
        return new V1_20_R2NbtTagCompound(toString());
    }

}
