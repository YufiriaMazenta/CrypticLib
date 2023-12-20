package crypticlib.nms.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.StringJoiner;

public abstract class NbtTagIntArray implements INbtTag<int[]>, INumberNbt {

    protected int[] value;

    public NbtTagIntArray(int[] value) {
        this.value = value;
    }

    public NbtTagIntArray(Object nmsNbtIntArray) {
        fromNms(nmsNbtIntArray);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.INT_ARRAY;
    }

    @Override
    public int @NotNull [] value() {
        return this.value;
    }

    @Override
    public void setValue(int @NotNull [] value) {
        this.value = value;
    }

    @Override
    public @NotNull JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (int i : value) {
            jsonArray.add(new JsonPrimitive(i));
        }
        return jsonArray;
    }

    @Override
    public String format() {
        StringJoiner str = new StringJoiner(",", "INT$[", "]");
        for (int i : value) {
            str.add(i + "");
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagIntArray that = (NbtTagIntArray) object;

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
