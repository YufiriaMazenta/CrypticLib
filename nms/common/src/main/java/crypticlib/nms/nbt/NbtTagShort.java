package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import crypticlib.util.JsonUtil;

import java.util.Objects;

public abstract class NbtTagShort implements INbtTag<Short>, INumberNbt {

    private Short value;

    public NbtTagShort(short value) {
        this.value = value;
    }

    public NbtTagShort(Object nmsNbtShort) {
        fromNms(nmsNbtShort);
    }

    @Override
    public NbtType type() {
        return NbtType.SHORT;
    }

    @Override
    public Short value() {
        return value;
    }

    @Override
    public void setValue(Short value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String format() {
        return "SHORT@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagShort that = (NbtTagShort) object;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return toNms().toString();
    }

}
