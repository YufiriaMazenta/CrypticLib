package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

import java.util.Objects;

public abstract class NbtTagString implements INbtTag<String> {

    private String value;

    public NbtTagString(String value) {
        this.value = value;
    }

    public NbtTagString(Object nmsNbtString) {
        fromNms(nmsNbtString);
    }

    @Override
    public NbtType type() {
        return NbtType.STRING;
    }

    @Override
    public String value() {
        return this.value;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
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

}
