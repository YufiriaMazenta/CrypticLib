package crypticlib.nms.nbt.v1_13_R1;

import crypticlib.nms.nbt.AbstractNbtTagList;
import crypticlib.nms.nbt.INbtTag;
import net.minecraft.server.v1_13_R1.NBTBase;
import net.minecraft.server.v1_13_R1.NBTTagList;

import java.util.List;

public class V1_13_R1NbtTagList extends AbstractNbtTagList {

    public V1_13_R1NbtTagList(List<Object> nbtList) {
        super(nbtList, V1_13_R1NbtTranslator.INSTANCE);
    }

    public V1_13_R1NbtTagList(Object nmsNbtList) {
        super(nmsNbtList, V1_13_R1NbtTranslator.INSTANCE);
    }

    public V1_13_R1NbtTagList() {
        super(V1_13_R1NbtTranslator.INSTANCE);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagList nbtTagList = (NBTTagList) nmsNbt;
        for (NBTBase nbtBase : nbtTagList) {
            value().add(getNbtTranslator().fromNms(nbtBase));
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
