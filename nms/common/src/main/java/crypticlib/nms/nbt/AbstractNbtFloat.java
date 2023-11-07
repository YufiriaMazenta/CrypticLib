package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

public abstract class AbstractNbtFloat implements INbtTag<Float>, IFormatNbtNumber {

    private Float value;

    public AbstractNbtFloat(float value) {
        this.value = value;
    }

    public AbstractNbtFloat(Object nmsNbtFloat) {
        fromNms(nmsNbtFloat);
    }

    @Override
    public NbtType type() {
        return NbtType.FLOAT;
    }

    @Override
    public Float value() {
        return this.value;
    }

    @Override
    public void setValue(Float value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String formatString() {
        return "FLOAT@" + value;
    }

}
