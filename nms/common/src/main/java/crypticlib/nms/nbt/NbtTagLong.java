package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class NbtTagLong implements INbtTag<Long>, INumberNbt {

    protected Long value;

    public NbtTagLong(long value) {
        this.value = value;
    }

    public NbtTagLong(Object nmsNbtLong) {
        fromNms(nmsNbtLong);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.LONG;
    }

    @Override
    public @NotNull Long value() {
        return value;
    }

    @Override
    public NbtTagLong setValue(@NotNull Long value) {
        this.value = value;
        return this;
    }

    @Override
    public @NotNull JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String formatValue() {
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
        return toNms().toString();
    }

}
