package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class NbtTagShort implements INbtTag<Short>, INumberNbt {

    protected Short value;

    public NbtTagShort(short value) {
        this.value = value;
    }

    public NbtTagShort(Object nmsNbtShort) {
        fromNms(nmsNbtShort);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.SHORT;
    }

    @Override
    public @NotNull Short value() {
        return value;
    }

    @Override
    public NbtTagShort setValue(@NotNull Short value) {
        this.value = value;
        return this;
    }

    @Override
    public @NotNull JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String formatValue() {
        return "SHORT@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagShort that = (NbtTagShort) object;

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
