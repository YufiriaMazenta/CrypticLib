package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;
import org.jetbrains.annotations.NotNull;

public abstract class NbtTagByte implements INbtTag<Byte>, INumberNbt {

    protected Byte value;

    public NbtTagByte(byte value) {
        this.value = value;
    }

    public NbtTagByte(Object nmsNbtByte) {
        fromNms(nmsNbtByte);
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.BYTE;
    }

    @Override
    public @NotNull Byte value() {
        return value;
    }

    @Override
    public NbtTagByte setValue(@NotNull Byte value) {
        this.value = value;
        return this;
    }

    @Override
    public @NotNull JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String formatValue() {
        return "BYTE@" + value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagByte that = (NbtTagByte) object;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return toNms().toString();
    }

}
