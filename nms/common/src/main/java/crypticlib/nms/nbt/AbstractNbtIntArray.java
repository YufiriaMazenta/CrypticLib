package crypticlib.nms.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.util.StringJoiner;

public abstract class AbstractNbtIntArray implements INbtTag<int[]>, IFormatNbtNumber {

    private int[] value;

    public AbstractNbtIntArray(int[] value) {
        this.value = value;
    }

    public AbstractNbtIntArray(Object nmsNbtIntArray) {
        fromNms(nmsNbtIntArray);
    }

    @Override
    public NbtType type() {
        return NbtType.INT_ARRAY;
    }

    @Override
    public int[] value() {
        return this.value;
    }

    @Override
    public void setValue(int[] value) {
        this.value = value;
    }

    @Override
    public JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (int i : value) {
            jsonArray.add(new JsonPrimitive(i));
        }
        return jsonArray;
    }

    @Override
    public String formatString() {
        StringJoiner str = new StringJoiner(",", "INT$[", "]");
        for (int i : value) {
            str.add(i + "");
        }
        return str.toString();
    }
}
