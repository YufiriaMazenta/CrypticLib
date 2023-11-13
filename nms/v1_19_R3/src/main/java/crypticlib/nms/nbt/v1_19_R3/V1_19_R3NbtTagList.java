package crypticlib.nms.nbt.v1_19_R3;

import crypticlib.nms.nbt.INbtTag;
import crypticlib.nms.nbt.NbtTagList;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import java.util.List;

public class V1_19_R3NbtTagList extends NbtTagList {

    public V1_19_R3NbtTagList(List<Object> nbtList) {
        super(nbtList, V1_19_R3NbtTranslator.INSTANCE);
    }

    public V1_19_R3NbtTagList(Object nmsNbtList) {
        super(nmsNbtList, V1_19_R3NbtTranslator.INSTANCE);
    }

    public V1_19_R3NbtTagList() {
        super(V1_19_R3NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagList nbtTagList = (NBTTagList) nmsNbt;
        for (NBTBase nbtBase : nbtTagList) {
            value().add(nbtTranslator().translateNmsNbt(nbtBase));
        }
    }

    @Override
    public NBTTagList toNms() {
        NBTTagList nbtTagList = new NBTTagList();
        for (INbtTag<?> nbtTag : value()) {
            nbtTagList.add((NBTBase) nbtTag.toNms());
        }
        return nbtTagList;
    }

}
