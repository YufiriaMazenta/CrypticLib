package crypticlib.nms.nbt;

import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;

import java.util.StringJoiner;

public abstract class AbstractNbtByteArray implements INbtTag<byte[]>, IFormatNbtNumber {

    private byte[] value;

    public AbstractNbtByteArray(byte[] value) {
        this.value = value;
    }

    public AbstractNbtByteArray(Object object) {
        fromNms(object);
    }

    @Override
    public NbtType type() {
        return NbtType.BYTE_ARRAY;
    }

    @Override
    public byte[] value() {
        return this.value;
    }

    @Override
    public void setValue(byte[] value) {
        this.value = value;
    }

    @Override
    public JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (byte b : value) {
            jsonArray.add(new JsonPrimitive(b));
        }
        return jsonArray;
    }

    @Override
    public String formatString() {
        StringJoiner str = new StringJoiner(",", "BYTE$[", "]");
        for (byte b : value) {
            str.add(b + "");
        }
        return str.toString();
    }

}
