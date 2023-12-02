package crypticlib.nms.nbt.v1_16_R3;

import crypticlib.nms.nbt.INbtTag;
import crypticlib.nms.nbt.INbtTranslator;
import net.minecraft.server.v1_16_R3.NBTBase;

import java.util.List;
import java.util.Map;

public enum V1_16_R3NbtTranslator implements INbtTranslator {

    INSTANCE;

    @Override
    public INbtTag<?> translateNmsNbt(Object nmsNbt) {
        switch (((NBTBase) nmsNbt).getTypeId()) {
            case 1:
                return new V1_16_R3NbtTagByte(nmsNbt);
            case 2:
                return new V1_16_R3NbtTagShort(nmsNbt);
            case 3:
                return new V1_16_R3NbtTagInt(nmsNbt);
            case 4:
                return new V1_16_R3NbtTagLong(nmsNbt);
            case 5:
                return new V1_16_R3NbtTagFloat(nmsNbt);
            case 6:
                return new V1_16_R3NbtTagDouble(nmsNbt);
            case 7:
                return new V1_16_R3NbtTagByteArray(nmsNbt);
            case 8:
                return new V1_16_R3NbtTagString(nmsNbt);
            case 9:
                return new V1_16_R3NbtTagList(nmsNbt);
            case 10:
                return new V1_16_R3NbtTagCompound(nmsNbt);
            case 11:
                return new V1_16_R3NbtTagIntArray(nmsNbt);
            case 12:
                return new V1_16_R3NbtTagLongArray(nmsNbt);
        }
        return null;
    }

    @Override
    public INbtTag<?> translateObject(Object object) {
        if (object instanceof Byte) {
            return new V1_16_R3NbtTagByte((byte) object);
        } else if (object instanceof byte[]) {
            return new V1_16_R3NbtTagByteArray((byte[]) object);
        } else if (object instanceof Map) {
            return new V1_16_R3NbtTagCompound(((Map<String, Object>) object));
        } else if (object instanceof Double) {
            return new V1_16_R3NbtTagDouble(((double) object));
        } else if (object instanceof Float) {
            return new V1_16_R3NbtTagFloat(((float) object));
        } else if (object instanceof Integer) {
            return new V1_16_R3NbtTagInt((int) object);
        } else if (object instanceof int[]) {
            return new V1_16_R3NbtTagIntArray(((int[]) object));
        } else if (object instanceof Long) {
            return new V1_16_R3NbtTagLong((long) object);
        } else if (object instanceof long[]) {
            return new V1_16_R3NbtTagLongArray((long[]) object);
        } else if (object instanceof Short) {
            return new V1_16_R3NbtTagShort((short) object);
        } else if (object instanceof String) {
            return new V1_16_R3NbtTagString((String) object);
        } else if (object instanceof List) {
            return new V1_16_R3NbtTagList((List<Object>) object);
        } else if (object instanceof INbtTag) {
            return (INbtTag<?>) object;
        } else {
            return translateNmsNbt(object);
        }
    }

}
