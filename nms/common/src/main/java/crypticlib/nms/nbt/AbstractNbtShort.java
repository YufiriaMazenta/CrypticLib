package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

import java.util.Objects;

public abstract class AbstractNbtShort implements INbtTag<Short>, INumberNbt {

    private Short value;

    public AbstractNbtShort(short value) {
        this.value = value;
    }

    public AbstractNbtShort(Object nmsNbtShort) {
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

        AbstractNbtShort that = (AbstractNbtShort) object;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

}
