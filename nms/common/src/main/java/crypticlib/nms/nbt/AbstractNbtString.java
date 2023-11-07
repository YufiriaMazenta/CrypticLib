package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

public abstract class AbstractNbtString implements INbtTag<String> {

    private String value;

    public AbstractNbtString(String value) {
        this.value = value;
    }

    public AbstractNbtString(Object nmsNbtString) {
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

}
