package crypticlib.nms.nbt;

import com.google.gson.JsonElement;
import org.jetbrains.annotations.NotNull;

/**
 * CrypticLib提供的nbt基础类型,等效于NMS的NBTBase
 */
public interface INbtTag<T> {

    /**
     * 获取nbt的类型
     *
     * @return nbt的类型
     */
    @NotNull NbtType type();

    /**
     * 获取nbt的值
     *
     * @return nbt的值
     */
    @NotNull T value();

    /**
     * 设置nbt的值
     *
     * @param value 设置的值
     */
    INbtTag<?> setValue(@NotNull T value);

    /**
     * 从nms的nbt对象中加载到此nbt对象
     *
     * @param nmsNbt nms的nbt对象
     */
    void fromNms(@NotNull Object nmsNbt);

    /**
     * 转化为nms的nbt对象
     *
     * @return 转化的nms nbt对象
     */
    @NotNull
    Object toNms();

    /**
     * 转化为json对象
     *
     * @return 转化的json对象
     */
    @NotNull
    JsonElement toJson();

}
