package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class NbtTagInt implements INbtTag<Integer>, INumberNbt {

    protected Integer value;

    public NbtTagInt(int value) {
        this.value = value;
    }

    public NbtTagInt(Object nmsNbtInt) {
        fromNms(nmsNbtInt);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.INT;
    }

    @Override
    public @NotNull Integer value() {
        return this.value;
    }

    @Override
    public NbtTagInt setValue(@NotNull Integer value) {
        this.value = value;
        return this;
    }

    @Override
    public @NotNull JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String formatValue() {
        return "INT@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagInt that = (NbtTagInt) object;

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
