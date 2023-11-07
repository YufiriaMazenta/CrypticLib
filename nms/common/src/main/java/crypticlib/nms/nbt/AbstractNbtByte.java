package crypticlib.nms.nbt;

import com.google.gson.JsonPrimitive;

public abstract class AbstractNbtByte implements INbtTag<Byte>, IFormatNbtNumber {

    private Byte value;

    public AbstractNbtByte(byte value) {
        this.value = value;
    }

    public AbstractNbtByte(Object nmsNbtByte) {
        fromNms(nmsNbtByte);
    }

    @Override
    public NbtType type() {
        return NbtType.BYTE;
    }

    @Override
    public Byte value() {
        return value;
    }

    @Override
    public void setValue(Byte value) {
        this.value = value;
    }

    @Override
    public JsonPrimitive toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public String formatString() {
        return "BYTE@" + value;
    }

}
