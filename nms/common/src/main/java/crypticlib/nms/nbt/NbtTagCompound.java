package crypticlib.nms.nbt;

import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class NbtTagCompound implements INbtTag<Map<String, INbtTag<?>>> {

    private INbtTranslator nbtTranslator;
    private Map<String, INbtTag<?>> nbtMap;

    public NbtTagCompound(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        this.nbtMap = new ConcurrentHashMap<>();
    }

    public NbtTagCompound(Object nmsNbtCompound, INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        this.nbtMap = new ConcurrentHashMap<>();
        fromNms(nmsNbtCompound);
    }

    public NbtTagCompound(Map<String, Object> nbtMap, INbtTranslator nbtTranslator) {
        this.nbtMap = new ConcurrentHashMap<>();
        this.nbtTranslator = nbtTranslator;
        nbtMap.forEach((key, val) -> this.nbtMap.put(key, nbtTranslator.translateObject(val)));
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.COMPOUND;
    }

    @Override
    public @NotNull Map<String, INbtTag<?>> value() {
        return nbtMap;
    }

    @Override
    public void setValue(@NotNull Map<String, INbtTag<?>> value) {
        this.nbtMap = value;
    }

    public NbtTagCompound set(String key, INbtTag<?> nbt) {
        nbtMap.put(key, nbt);
        return this;
    }

    public NbtTagCompound set(String key, Byte value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, byte[] value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, Double value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, Float value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, Integer value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, int[] value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, Long value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, long[] value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, Short value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, String value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, Map<String, Object> value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound set(String key, List<Object> value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagCompound remove(String key) {
        nbtMap.remove(key);
        return this;
    }

    public NbtTagCompound clear() {
        nbtMap.clear();
        return this;
    }

    /**
     * 与另外一个NbtTagCompound合并
     *
     * @param other   另外一个NbtTagCompound
     * @param rewrite 当出现相同的key时，是否让另外一个重写本身的nbt
     * @return 自身（合并完毕的NbtTagCompound）
     */
    public NbtTagCompound merge(NbtTagCompound other, boolean rewrite) {
        other.value().forEach((key, nbt) -> {
            if (value().containsKey(key)) {
                if (value().get(key) instanceof NbtTagCompound && nbt instanceof NbtTagCompound) {
                    ((NbtTagCompound) value().get(key)).merge((NbtTagCompound) nbt, rewrite);
                    return;
                }
                if (rewrite)
                    value().put(key, nbt);
            } else {
                value().put(key, nbt);
            }
        });
        return this;
    }

    public Boolean contains(String key) {
        return nbtMap.containsKey(key);
    }

    public INbtTag<?> get(String key) {
        return nbtMap.get(key);
    }

    public INbtTranslator nbtTranslator() {
        return nbtTranslator;
    }

    public NbtTagCompound setNbtTranslator(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        return this;
    }

    @Override
    public @NotNull JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        value().forEach((key, val) -> jsonObject.add(key, val.toJson()));
        return jsonObject;
    }

    public Map<String, Object> unwarppedMap() {
        Map<String, Object> map = new HashMap<>();
        nbtMap.forEach((key, nbtTag) -> {
            if (nbtTag instanceof NbtTagCompound) {
                map.put(key, ((NbtTagCompound) nbtTag).unwarppedMap());
            } else if (nbtTag instanceof NbtTagList) {
                map.put(key, ((NbtTagList) nbtTag).unwrappedList());
            } else if (nbtTag instanceof INumberNbt) {
                map.put(key, ((INumberNbt) nbtTag).format());
            } else {
                map.put(key, nbtTag.value());
            }
        });
        return map;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        NbtTagCompound that = (NbtTagCompound) object;

        return Objects.equals(nbtMap, that.nbtMap);
    }

    @Override
    public int hashCode() {
        return nbtMap != null ? nbtMap.hashCode() : 0;
    }

    @Override
    public String toString() {
        return toNms().toString();
    }

}
