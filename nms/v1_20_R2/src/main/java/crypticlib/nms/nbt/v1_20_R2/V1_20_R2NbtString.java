package crypticlib.nms.nbt.v1_20_R2;

import crypticlib.nms.nbt.AbstractNbtString;
import net.minecraft.nbt.*;

public class V1_20_R2NbtString extends AbstractNbtString {

    public V1_20_R2NbtString(String value) {
        super(value);
    }

    public V1_20_R2NbtString(Object nmsNbtString) {
        super(nmsNbtString);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagString nbtTagString = (NBTTagString) nmsNbt;
        setValue(nbtTagString.toString());
    }

    @Override
    public NBTBase toNms() {
        if (value().contains("@")) {
            return parseNumberNbt();
        } else if (value().contains("$")) {
            return parseNumberArrayNbt();
        }
        return NBTTagString.a(value());
    }

    private NBTBase parseNumberArrayNbt() {
        String[] split = value().split("\\$");
        if (split.length != 2)
            return NBTTagString.a(value());
        String type = split[0];
        String value = split[1];
        value = value.replace("[", "").replace("]", "");
        String[] valueList = value.split(",");
        try {
            switch (type) {
                case "BYTE":
                    byte[] byteArr = new byte[valueList.length];
                    for (int i = 0; i < valueList.length; i++) {
                        byteArr[i] = Byte.parseByte(valueList[i]);
                    }
                    return new NBTTagByteArray(byteArr);
                case "LONG":
                    long[] longArr = new long[valueList.length];
                    for (int i = 0; i < valueList.length; i++) {
                        longArr[i] = Long.parseLong(valueList[i]);
                    }
                    return new NBTTagLongArray(longArr);
                case "INT":
                    int[] intArr = new int[valueList.length];
                    for (int i = 0; i < valueList.length; i++) {
                        intArr[i] = Integer.parseInt(valueList[i]);
                    }
                    return new NBTTagIntArray(intArr);
                default:
                    return NBTTagString.a(value());
            }
        } catch (NumberFormatException e) {
            return NBTTagString.a(value());
        }
    }

    private NBTBase parseNumberNbt() {
        String[] split = value().split("@");
        if (split.length != 2)
            return NBTTagString.a(value());
        String type = split[0];
        String value = split[1];
        try {
            switch (type) {
                case "BYTE":
                    return NBTTagByte.a(Byte.parseByte(value));
                case "FLOAT":
                    return NBTTagFloat.a(Float.parseFloat(value));
                case "LONG":
                    return NBTTagLong.a(Long.parseLong(value));
                case "SHORT":
                    return NBTTagShort.a(Short.parseShort(value));
                default:
                    return NBTTagString.a(value());
            }
        } catch (NumberFormatException e) {
            return NBTTagString.a(value());
        }
    }

}
