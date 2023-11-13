package crypticlib.nms.nbt.v1_12_R1;

import crypticlib.nms.nbt.INbtTag;
import crypticlib.nms.nbt.INbtTranslator;
import net.minecraft.server.v1_13_R1.NBTBase;

import java.util.List;
import java.util.Map;

public enum V1_12_R1NbtTranslator implements INbtTranslator {

    INSTANCE;

    @Override
    public INbtTag<?> translateNmsNbt(Object nmsNbt) {
        switch (((NBTBase) nmsNbt).getTypeId()) {
            case 1:
                return new V1_12_R1NbtTagByte(nmsNbt);
            case 2:
                return new V1_12_R1NbtTagShort(nmsNbt);
            case 3:
                return new V1_12_R1NbtTagInt(nmsNbt);
            case 4:
                return new V1_12_R1NbtTagLong(nmsNbt);
            case 5:
                return new V1_12_R1NbtTagFloat(nmsNbt);
            case 6:
                return new V1_12_R1NbtTagDouble(nmsNbt);
            case 7:
                return new V1_12_R1NbtTagByteArray(nmsNbt);
            case 8:
                return new V1_12_R1NbtTagString(nmsNbt);
            case 9:
                return new V1_12_R1NbtTagList(nmsNbt);
            case 10:
                return new V1_12_R1NbtTagCompound(nmsNbt);
            case 11:
                return new V1_12_R1NbtTagIntArray(nmsNbt);
            case 12:
                return new V1_12_R1NbtTagLongArray(nmsNbt);
        }
        return null;
    }

    @Override
    public INbtTag<?> translateObject(Object object) {
        if (object instanceof Byte) {
            return new V1_12_R1NbtTagByte((byte) object);
        } else if (object instanceof byte[]) {
            return new V1_12_R1NbtTagByteArray((byte[])object);
        } else if (object instanceof Map) {
            return new V1_12_R1NbtTagCompound(((Map<String, Object>) object));
        } else if (object instanceof Double) {
            return new V1_12_R1NbtTagDouble(((double) object));
        } else if (object instanceof Float) {
            return new V1_12_R1NbtTagFloat(((float) object));
        } else if (object instanceof Integer) {
            return new V1_12_R1NbtTagInt((int) object);
        } else if (object instanceof int[]) {
            return new V1_12_R1NbtTagIntArray(((int[]) object));
        } else if (object instanceof Long) {
            return new V1_12_R1NbtTagLong((long) object);
        } else if (object instanceof long[]) {
            return new V1_12_R1NbtTagLongArray((long[]) object);
        } else if (object instanceof Short) {
            return new V1_12_R1NbtTagShort((short) object);
        } else if (object instanceof String) {
            return new V1_12_R1NbtTagString((String) object);
        } else if (object instanceof List) {
            return new V1_12_R1NbtTagList((List<Object>) object);
        } else if (object instanceof INbtTag) {
            return (INbtTag<?>) object;
        } else {
            return translateNmsNbt(object);
        }
    }

}
