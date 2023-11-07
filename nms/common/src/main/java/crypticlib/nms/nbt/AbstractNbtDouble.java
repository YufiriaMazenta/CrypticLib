package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

public abstract class AbstractNbtDouble implements INbtTag<Double> {

    public AbstractNbtDouble(double value) {
        this.value = value;
    }

    public AbstractNbtDouble(Object nmsNbtDouble) {
        fromNms(nmsNbtDouble);
    }

    private Double value;

    @Override
    public NbtType type() {
        return NbtType.DOUBLE;
    }

    @Override
    public Double value() {
        return this.value;
    }

    @Override
    public void setValue(Double value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

}
