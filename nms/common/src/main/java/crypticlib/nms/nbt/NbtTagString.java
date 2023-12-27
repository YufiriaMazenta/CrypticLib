package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public abstract class NbtTagString implements INbtTag<String> {

    protected String value;

    public NbtTagString(String value) {
        this.value = value;
    }

    public NbtTagString(Object nmsNbtString) {
        fromNms(nmsNbtString);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.STRING;
    }

    @Override
    public @NotNull String value() {
        return this.value;
    }

    @Override
    public NbtTagString setValue(@NotNull String value) {
        this.value = value;
        return this;
    }

    @Override
    public @NotNull JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagString that = (NbtTagString) object;

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
