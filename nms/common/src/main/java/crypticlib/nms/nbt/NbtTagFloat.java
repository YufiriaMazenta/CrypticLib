package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import crypticlib.util.JsonUtil;

import java.util.Objects;

public abstract class NbtTagFloat implements INbtTag<Float>, INumberNbt {

    private Float value;

    public NbtTagFloat(float value) {
        this.value = value;
    }

    public NbtTagFloat(Object nmsNbtFloat) {
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

        NbtTagFloat that = (NbtTagFloat) object;

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
