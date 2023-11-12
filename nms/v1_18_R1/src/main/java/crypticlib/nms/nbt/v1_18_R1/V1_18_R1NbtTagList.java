package crypticlib.nms.nbt.v1_18_R1;

import crypticlib.nms.nbt.AbstractNbtTagList;
import crypticlib.nms.nbt.INbtTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;

import java.util.List;

public class V1_18_R1NbtTagList extends AbstractNbtTagList {

    public V1_18_R1NbtTagList(List<Object> nbtList) {
        super(nbtList, V1_18_R1NbtTranslator.INSTANCE);
    }

    public V1_18_R1NbtTagList(Object nmsNbtList) {
        super(nmsNbtList, V1_18_R1NbtTranslator.INSTANCE);
    }

    public V1_18_R1NbtTagList() {
        super(V1_18_R1NbtTranslator.INSTANCE);
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
