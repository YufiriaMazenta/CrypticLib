package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

import java.util.Objects;

public abstract class AbstractNbtFloat implements INbtTag<Float>, INumberNbt {

    private Float value;

    public AbstractNbtFloat(float value) {
        this.value = value;
    }

    public AbstractNbtFloat(Object nmsNbtFloat) {
        fromNms(nmsNbtFloat);
    }

    @Override
    public NbtType type() {
        return NbtType.FLOAT;
    }

    @Override
    public Float value() {
        return this.value;
    }

    @Override
    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String format() {
        return "FLOAT@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        AbstractNbtFloat that = (AbstractNbtFloat) object;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

}
