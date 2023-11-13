package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import crypticlib.util.JsonUtil;

import java.util.Objects;

public abstract class NbtTagDouble implements INbtTag<Double>, INumberNbt {

    public NbtTagDouble(double value) {
        this.value = value;
    }

    public NbtTagDouble(Object nmsNbtDouble) {
        fromNms(nmsNbtDouble);
    }

    private Double value;

    @Override
    public NbtType type() {
        return NbtType.DOUBLE;
    }

    @Override
    public Double value() {
        return this.value;
    }

    @Override
    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String format() {
        return "DOUBLE@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagDouble that = (NbtTagDouble) object;

        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
        return JsonUtil.json2Str(toJson());
    }

}
