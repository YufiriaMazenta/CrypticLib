package crypticlib.nms.nbt;

import com.google.gson.JsonElement;

public interface INbtTag<T> {

    NbtType type();

    T value();

    void setValue(T value);

    void fromNms(Object nmsNbt);

    Object toNms();

    JsonElement toJson();

}
