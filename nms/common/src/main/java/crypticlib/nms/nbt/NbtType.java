package crypticlib.nms.nbt;

public enum NbtType {

    INT(3),
    SHORT(2),
    BYTE(1),
    LONG(4),
    FLOAT(5),
    DOUBLE(6),
    STRING(8),
    COMPOUND(10),
    LIST(9),
    BYTE_ARRAY(7),
    INT_ARRAY(11),
    LONG_ARRAY(12),
    END(0);

    private final byte typeId;

    NbtType(int typeId) {
        this.typeId = (byte) typeId;
    }

    public byte typeId() {
        return typeId;
    }
}
