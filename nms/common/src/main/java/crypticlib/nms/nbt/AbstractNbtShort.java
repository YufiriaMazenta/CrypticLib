package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

public abstract class AbstractNbtShort implements INbtTag<Short>, IFormatNbtNumber {

    private Short value;

    public AbstractNbtShort(short value) {
        this.value = value;
    }

    public AbstractNbtShort(Object nmsNbtShort) {
        fromNms(nmsNbtShort);
    }

    @Override
    public NbtType type() {
        return NbtType.SHORT;
    }

    @Override
    public Short value() {
        return value;
    }

    @Override
    public void setValue(Short value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String formatString() {
        return "SHORT@" + value;
    }

}
