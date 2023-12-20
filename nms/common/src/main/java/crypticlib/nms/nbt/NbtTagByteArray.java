package crypticlib.nms.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.StringJoiner;

public abstract class NbtTagByteArray implements INbtTag<byte[]>, INumberNbt {

    protected byte[] value;

    public NbtTagByteArray(byte[] value) {
        this.value = value;
    }

    public NbtTagByteArray(Object object) {
        fromNms(object);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.BYTE_ARRAY;
    }

    @Override
    public byte @NotNull [] value() {
        return this.value;
    }

    @Override
    public void setValue(byte @NotNull [] value) {
        this.value = value;
    }

    @Override
    public @NotNull JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (byte b : value) {
            jsonArray.add(new JsonPrimitive(b));
        }
        return jsonArray;
    }

    @Override
    public String format() {
        StringJoiner str = new StringJoiner(",", "BYTE$[", "]");
        for (byte b : value) {
            str.add(b + "");
        }
        return str.toString();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagByteArray that = (NbtTagByteArray) object;

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
