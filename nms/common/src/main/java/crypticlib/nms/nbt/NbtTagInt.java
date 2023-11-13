package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

import java.util.Objects;

public abstract class NbtTagInt implements INbtTag<Integer>, INumberNbt {

    private Integer value;

    public NbtTagInt(int value) {
        this.value = value;
    }

    public NbtTagInt(Object nmsNbtInt) {
        fromNms(nmsNbtInt);
    }

    @Override
    public NbtType type() {
        return NbtType.INT;
    }

    @Override
    public Integer value() {
        return this.value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String format() {
        return "INT@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagInt that = (NbtTagInt) object;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

}
