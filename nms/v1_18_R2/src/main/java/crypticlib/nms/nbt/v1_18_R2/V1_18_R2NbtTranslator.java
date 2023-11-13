package crypticlib.nms.nbt.v1_18_R2;

import crypticlib.nms.nbt.INbtTag;
import crypticlib.nms.nbt.INbtTranslator;
import net.minecraft.nbt.NBTBase;

import java.util.List;
import java.util.Map;

public enum V1_18_R2NbtTranslator implements INbtTranslator {

    INSTANCE;

    @Override
    public INbtTag<?> translateNmsNbt(Object nmsNbt) {
        switch (((NBTBase) nmsNbt).a()) {
            case 1:
                return new V1_18_R2NbtTagByte(nmsNbt);
            case 2:
                return new V1_18_R2NbtTagShort(nmsNbt);
            case 3:
                return new V1_18_R2NbtTagInt(nmsNbt);
            case 4:
                return new V1_18_R2NbtTagLong(nmsNbt);
            case 5:
                return new V1_18_R2NbtTagFloat(nmsNbt);
            case 6:
                return new V1_18_R2NbtTagDouble(nmsNbt);
            case 7:
                return new V1_18_R2NbtTagByteArray(nmsNbt);
            case 8:
                return new V1_18_R2NbtTagString(nmsNbt);
            case 9:
                return new V1_18_R2NbtTagList(nmsNbt);
            case 10:
                return new V1_18_R2NbtTagCompound(nmsNbt);
            case 11:
                return new V1_18_R2NbtTagIntArray(nmsNbt);
            case 12:
                return new V1_18_R2NbtTagLongArray(nmsNbt);
        }
        return null;
    }

    @Override
    public INbtTag<?> translateObject(Object object) {
        if (object instanceof Byte) {
            return new V1_18_R2NbtTagByte((byte) object);
        } else if (object instanceof byte[]) {
            return new V1_18_R2NbtTagByteArray((byte[])object);
        } else if (object instanceof Map) {
            return new V1_18_R2NbtTagCompound(((Map<String, Object>) object));
        } else if (object instanceof Double) {
            return new V1_18_R2NbtTagDouble(((double) object));
        } else if (object instanceof Float) {
            return new V1_18_R2NbtTagFloat(((float) object));
        } else if (object instanceof Integer) {
            return new V1_18_R2NbtTagInt((int) object);
        } else if (object instanceof int[]) {
            return new V1_18_R2NbtTagIntArray(((int[]) object));
        } else if (object instanceof Long) {
            return new V1_18_R2NbtTagLong((long) object);
        } else if (object instanceof long[]) {
            return new V1_18_R2NbtTagLongArray((long[]) object);
        } else if (object instanceof Short) {
            return new V1_18_R2NbtTagShort((short) object);
        } else if (object instanceof String) {
            return new V1_18_R2NbtTagString((String) object);
        } else if (object instanceof List) {
            return new V1_18_R2NbtTagList((List<Object>) object);
        } else if (object instanceof INbtTag) {
            return (INbtTag<?>) object;
        } else {
            return translateNmsNbt(object);
        }
    }

}
