package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

public abstract class AbstractNbtLong implements INbtTag<Long>, IFormatNbtNumber {

    private Long value;

    public AbstractNbtLong(long value) {
        this.value = value;
    }

    public AbstractNbtLong(Object nmsNbtLong) {
        fromNms(nmsNbtLong);
    }

    @Override
    public NbtType type() {
        return NbtType.LONG;
    }

    @Override
    public Long value() {
        return value;
    }

    @Override
    public void setValue(Long value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String formatString() {
        return "LONG@" + value;
    }

}
