package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import crypticlib.util.JsonUtil;

public abstract class NbtTagByte implements INbtTag<Byte>, INumberNbt {

    private Byte value;

    public NbtTagByte(byte value) {
        this.value = value;
    }

    public NbtTagByte(Object nmsNbtByte) {
        fromNms(nmsNbtByte);
    }

    @Override
    public NbtType type() {
        return NbtType.BYTE;
    }

    @Override
    public Byte value() {
        return value;
    }

    @Override
    public void setValue(Byte value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String format() {
        return "BYTE@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagByte that = (NbtTagByte) object;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return toNms().toString();
    }

}
