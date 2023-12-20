package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.INbtTag;
import crypticlib.nms.nbt.NbtTagList;
import net.minecraft.server.v1_13_R2.NBTBase;
import net.minecraft.server.v1_13_R2.NBTTagList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class V1_13_R2NbtTagList extends NbtTagList {

    public V1_13_R2NbtTagList(List<Object> nbtList) {
        super(nbtList, V1_13_R2NbtTranslator.INSTANCE);
    }

    public V1_13_R2NbtTagList(Object nmsNbtList) {
        super(nmsNbtList, V1_13_R2NbtTranslator.INSTANCE);
    }

    public V1_13_R2NbtTagList() {
        super(V1_13_R2NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(@NotNull Object nmsNbt) {
        NBTTagList nbtTagList = (NBTTagList) nmsNbt;
        for (NBTBase nbtBase : nbtTagList) {
            nbtList.add(nbtTranslator().translateNmsNbt(nbtBase));
        }
    }

    @Override
    public @NotNull NBTTagList toNms() {
        NBTTagList nbtTagList = new NBTTagList();
        for (INbtTag<?> nbtTag : nbtList) {
            nbtTagList.add((NBTBase) nbtTag.toNms());
        }
        return nbtTagList;
    }

}
