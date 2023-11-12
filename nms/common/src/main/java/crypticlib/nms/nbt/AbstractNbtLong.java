package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

import java.util.Objects;

public abstract class AbstractNbtLong implements INbtTag<Long>, INumberNbt {

    private Long value;

    public AbstractNbtLong(long value) {
        this.value = value;
    }

    public AbstractNbtLong(Object nmsNbtLong) {
        fromNms(nmsNbtLong);
    }

    @Override
    public NbtType type() {
        return NbtType.LONG;
    }

    @Override
    public Long value() {
        return value;
    }

    @Override
    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String format() {
        return "LONG@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        AbstractNbtLong that = (AbstractNbtLong) object;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

}
