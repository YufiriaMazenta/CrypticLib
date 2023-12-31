package crypticlib.nms.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.StringJoiner;

public abstract class NbtTagLongArray implements INbtTag<long[]>, INumberNbt {

    protected long[] value;

    public NbtTagLongArray(long[] value) {
        this.value = value;
    }

    public NbtTagLongArray(Object nmsNbtLongArray) {
        fromNms(nmsNbtLongArray);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.LONG_ARRAY;
    }

    @Override
    public long @NotNull [] value() {
        return this.value;
    }

    @Override
    public NbtTagLongArray setValue(long @NotNull [] value) {
        this.value = value;
        return this;
    }

    @Override
    public @NotNull JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (long l : value) {
            jsonArray.add(new JsonPrimitive(l));
        }
        return jsonArray;
    }

    @Override
    public String formatValue() {
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
        return toNms().toString();
    }

}
