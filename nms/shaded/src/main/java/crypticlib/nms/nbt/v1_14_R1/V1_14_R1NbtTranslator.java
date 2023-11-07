package crypticlib.nms.nbt.v1_14_R1;

import crypticlib.nms.nbt.INbtTag;
import crypticlib.nms.nbt.INbtTranslator;
import net.minecraft.server.v1_14_R1.NBTBase;

import java.util.List;
import java.util.Map;

public enum V1_14_R1NbtTranslator implements INbtTranslator {

    INSTANCE;

    @Override
    public INbtTag<?> fromNms(Object nmsNbt) {
        switch (((NBTBase) nmsNbt).getTypeId()) {
            case 1:
                return new V1_14_R1NbtByte(nmsNbt);
            case 2:
                return new V1_14_R1NbtShort(nmsNbt);
            case 3:
                return new V1_14_R1NbtInt(nmsNbt);
            case 4:
                return new V1_14_R1NbtLong(nmsNbt);
            case 5:
                return new V1_14_R1NbtFloat(nmsNbt);
            case 6:
                return new V1_14_R1NbtDouble(nmsNbt);
            case 7:
                return new V1_14_R1NbtByteArray(nmsNbt);
            case 8:
                return new V1_14_R1NbtString(nmsNbt);
            case 9:
                return new V1_14_R1NbtTagList(nmsNbt);
            case 10:
                return new V1_14_R1NbtCompound(nmsNbt);
            case 11:
                return new V1_14_R1NbtIntArray(nmsNbt);
            case 12:
                return new V1_14_R1NbtLongArray(nmsNbt);
        }
        return null;
    }

    @Override
    public INbtTag<?> fromObject(Object object) {
        if (object instanceof Byte) {
            return new V1_14_R1NbtByte((byte) object);
        } else if (object instanceof byte[]) {
            return new V1_14_R1NbtByteArray((byte[])object);
        } else if (object instanceof Map) {
            return new V1_14_R1NbtCompound(((Map<String, Object>) object));
        } else if (object instanceof Double) {
            return new V1_14_R1NbtDouble(((double) object));
        } else if (object instanceof Float) {
            return new V1_14_R1NbtFloat(((float) object));
        } else if (object instanceof Integer) {
            return new V1_14_R1NbtInt((int) object);
        } else if (object instanceof int[]) {
            return new V1_14_R1NbtIntArray(((int[]) object));
        } else if (object instanceof Long) {
            return new V1_14_R1NbtLong((long) object);
        } else if (object instanceof long[]) {
            return new V1_14_R1NbtLongArray((long[]) object);
        } else if (object instanceof Short) {
            return new V1_14_R1NbtShort((short) object);
        } else if (object instanceof String) {
            return new V1_14_R1NbtString((String) object);
        } else if (object instanceof List) {
            return new V1_14_R1NbtTagList((List<Object>) object);
        } else if (object instanceof INbtTag) {
            return (INbtTag<?>) object;
        } else {
            return fromNms(object);
        }
    }

}
