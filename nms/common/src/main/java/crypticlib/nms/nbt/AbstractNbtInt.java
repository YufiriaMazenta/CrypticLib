package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

public abstract class AbstractNbtInt implements INbtTag<Integer> {

    private Integer value;

    public AbstractNbtInt(int value) {
        this.value = value;
    }

    public AbstractNbtInt(Object nmsNbtInt) {
        fromNms(nmsNbtInt);
    }

    @Override
    public NbtType type() {
        return NbtType.INT;
    }

    @Override
    public Integer value() {
        return this.value;
    }

    @Override
    public void setValue(Integer value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

}
