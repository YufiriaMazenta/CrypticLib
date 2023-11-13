package crypticlib.nms.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import crypticlib.util.JsonUtil;

import java.util.Arrays;
import java.util.StringJoiner;

public abstract class NbtTagLongArray implements INbtTag<long[]>, INumberNbt {

    private long[] value;

    public NbtTagLongArray(long[] value) {
        this.value = value;
    }

    public NbtTagLongArray(Object nmsNbtLongArray) {
        fromNms(nmsNbtLongArray);
    }

    @Override
    public NbtType type() {
        return NbtType.LONG_ARRAY;
    }

    @Override
    public long[] value() {
        return this.value;
    }

    @Override
    public void setValue(long[] value) {
        this.value = value;
    }

    @Override
    public JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (long l: value) {
            jsonArray.add(new JsonPrimitive(l));
        }
        return jsonArray;
    }

    @Override
    public String format() {
        StringJoiner str = new StringJoiner(",", "LONG$[", "]");
        for (long l : value) {
            str.add(l + "");
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagLongArray that = (NbtTagLongArray) object;

        return Arrays.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public String toString() {
        return JsonUtil.json2Str(toJson());
    }

}
