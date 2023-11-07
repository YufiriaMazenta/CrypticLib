package crypticlib.nms.nbt.v1_13_R2;

import crypticlib.nms.nbt.INbtTag;
import crypticlib.nms.nbt.INbtTranslator;
import net.minecraft.server.v1_13_R2.NBTBase;

import java.util.List;
import java.util.Map;

public enum V1_13_R2NbtTranslator implements INbtTranslator {

    INSTANCE;

    @Override
    public INbtTag<?> fromNms(Object nmsNbt) {
        switch (((NBTBase) nmsNbt).getTypeId()) {
            case 1:
                return new V1_13_R2NbtByte(nmsNbt);
            case 2:
                return new V1_13_R2NbtShort(nmsNbt);
            case 3:
                return new V1_13_R2NbtInt(nmsNbt);
            case 4:
                return new V1_13_R2NbtLong(nmsNbt);
            case 5:
                return new V1_13_R2NbtFloat(nmsNbt);
            case 6:
                return new V1_13_R2NbtDouble(nmsNbt);
            case 7:
                return new V1_13_R2NbtByteArray(nmsNbt);
            case 8:
                return new V1_13_R2NbtString(nmsNbt);
            case 9:
                return new V1_13_R2NbtTagList(nmsNbt);
            case 10:
                return new V1_13_R2NbtCompound(nmsNbt);
            case 11:
                return new V1_13_R2NbtIntArray(nmsNbt);
            case 12:
                return new V1_13_R2NbtLongArray(nmsNbt);
        }
        return null;
    }

    @Override
    public INbtTag<?> fromObject(Object object) {
        if (object instanceof Byte) {
            return new V1_13_R2NbtByte((byte) object);
        } else if (object instanceof byte[]) {
            return new V1_13_R2NbtByteArray((byte[])object);
        } else if (object instanceof Map) {
            return new V1_13_R2NbtCompound(((Map<String, Object>) object));
        } else if (object instanceof Double) {
            return new V1_13_R2NbtDouble(((double) object));
        } else if (object instanceof Float) {
            return new V1_13_R2NbtFloat(((float) object));
        } else if (object instanceof Integer) {
            return new V1_13_R2NbtInt((int) object);
        } else if (object instanceof int[]) {
            return new V1_13_R2NbtIntArray(((int[]) object));
        } else if (object instanceof Long) {
            return new V1_13_R2NbtLong((long) object);
        } else if (object instanceof long[]) {
            return new V1_13_R2NbtLongArray((long[]) object);
        } else if (object instanceof Short) {
            return new V1_13_R2NbtShort((short) object);
        } else if (object instanceof String) {
            return new V1_13_R2NbtString((String) object);
        } else if (object instanceof List) {
            return new V1_13_R2NbtTagList((List<Object>) object);
        } else if (object instanceof INbtTag) {
            return (INbtTag<?>) object;
        } else {
            return fromNms(object);
        }
    }

}
