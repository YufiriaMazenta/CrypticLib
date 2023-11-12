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
                return new V1_16_R3NbtByte(nmsNbt);
            case 2:
                return new V1_16_R3NbtShort(nmsNbt);
            case 3:
                return new V1_16_R3NbtInt(nmsNbt);
            case 4:
                return new V1_16_R3NbtLong(nmsNbt);
            case 5:
                return new V1_16_R3NbtFloat(nmsNbt);
            case 6:
                return new V1_16_R3NbtDouble(nmsNbt);
            case 7:
                return new V1_16_R3NbtByteArray(nmsNbt);
            case 8:
                return new V1_16_R3NbtString(nmsNbt);
            case 9:
                return new V1_16_R3NbtTagList(nmsNbt);
            case 10:
                return new V1_16_R3NbtTagCompound(nmsNbt);
            case 11:
                return new V1_16_R3NbtIntArray(nmsNbt);
            case 12:
                return new V1_16_R3NbtLongArray(nmsNbt);
        }
        return null;
    }
    
    @Override
    public INbtTag<?> translateObject(Object object) {
        if (object instanceof Byte) {
            return new V1_16_R3NbtByte((byte) object);
        } else if (object instanceof byte[]) {
            return new V1_16_R3NbtByteArray((byte[])object);
        } else if (object instanceof Map) {
            return new V1_16_R3NbtTagCompound(((Map<String, Object>) object));
        } else if (object instanceof Double) {
            return new V1_16_R3NbtDouble(((double) object));
        } else if (object instanceof Float) {
            return new V1_16_R3NbtFloat(((float) object));
        } else if (object instanceof Integer) {
            return new V1_16_R3NbtInt((int) object);
        } else if (object instanceof int[]) {
            return new V1_16_R3NbtIntArray(((int[]) object));
        } else if (object instanceof Long) {
            return new V1_16_R3NbtLong((long) object);
        } else if (object instanceof long[]) {
            return new V1_16_R3NbtLongArray((long[]) object);
        } else if (object instanceof Short) {
            return new V1_16_R3NbtShort((short) object);
        } else if (object instanceof String) {
            return new V1_16_R3NbtString((String) object);
        } else if (object instanceof List) {
            return new V1_16_R3NbtTagList((List<Object>) object);
        } else if (object instanceof INbtTag) {
            return (INbtTag<?>) object;
        } else {
            return translateNmsNbt(object);
        }
    }
    
}
