package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class NbtTagFloat implements INbtTag<Float>, INumberNbt {

    protected Float value;

    public NbtTagFloat(float value) {
        this.value = value;
    }

    public NbtTagFloat(Object nmsNbtFloat) {
        fromNms(nmsNbtFloat);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.FLOAT;
    }

    @Override
    public @NotNull Float value() {
        return this.value;
    }

    @Override
    public void setValue(@NotNull Float value) {
        this.value = value;
    }

    @Override
    public @NotNull JsonPrimitive toJson() {
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
        return toNms().toString();
    }

}
