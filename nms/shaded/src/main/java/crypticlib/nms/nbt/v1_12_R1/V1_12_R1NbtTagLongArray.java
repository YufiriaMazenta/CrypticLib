package crypticlib.nms.nbt.v1_12_R1;

import crypticlib.nms.nbt.NbtTagLongArray;
import net.minecraft.server.v1_12_R1.NBTTagLongArray;

import java.lang.reflect.Field;

public class V1_12_R1NbtTagLongArray extends NbtTagLongArray {

    public V1_12_R1NbtTagLongArray(long[] value) {
        super(value);
    }

    public V1_12_R1NbtTagLongArray(Object nmsNbtLongArray) {
        super(nmsNbtLongArray);
    }

    @Override
    public void fromNms(Object nmsNbt) {
        NBTTagLongArray nbtTagLongArray = (NBTTagLongArray) nmsNbt;
        try {
            Field valueField = nbtTagLongArray.getClass().getField("b");
            long[] value = (long[]) valueField.get(nbtTagLongArray);
            setValue(value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            setValue(new long[]{});
            e.printStackTrace();
        }
    }

    @Override
    public NBTTagLongArray toNms() {
        return new NBTTagLongArray(value());
    }
}
