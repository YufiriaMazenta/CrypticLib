package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import crypticlib.util.JsonUtil;

import java.util.Objects;

public abstract class NbtTagLong implements INbtTag<Long>, INumberNbt {

    private Long value;

    public NbtTagLong(long value) {
        this.value = value;
    }

    public NbtTagLong(Object nmsNbtLong) {
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

        NbtTagLong that = (NbtTagLong) object;

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
