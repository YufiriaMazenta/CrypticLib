package crypticlib.nms.nbt;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractNbtTagCompound implements INbtTag<Map<String, INbtTag<?>>> {

    private INbtTranslator nbtTranslator;
    private Map<String, INbtTag<?>> nbtMap;

    public AbstractNbtTagCompound(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        this.nbtMap = new ConcurrentHashMap<>();
    }

    public AbstractNbtTagCompound(Object nmsNbtCompound, INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        this.nbtMap = new ConcurrentHashMap<>();
        fromNms(nmsNbtCompound);
    }

    public AbstractNbtTagCompound(Map<String, Object> nbtMap, INbtTranslator nbtTranslator) {
        this.nbtMap = new ConcurrentHashMap<>();
        this.nbtTranslator = nbtTranslator;
        nbtMap.forEach((key, val) -> {
            this.nbtMap.put(key, nbtTranslator.translateObject(val));
        });
    }

    @Override
    public NbtType type() {
        return NbtType.COMPOUND;
    }

    @Override
    public Map<String, INbtTag<?>> value() {
        return nbtMap;
    }

    @Override
    public void setValue(Map<String, INbtTag<?>> value) {
        this.nbtMap = value;
    }

    public AbstractNbtTagCompound set(String key, INbtTag<?> nbt) {
        nbtMap.put(key, nbt);
        return this;
    }

    public AbstractNbtTagCompound set(String key, Byte value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, byte[] value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, Double value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, Float value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, Integer value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, int[] value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, Long value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, long[] value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, Short value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, String value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, Map<String, Object> value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound set(String key, List<Object> value) {
        nbtMap.put(key, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagCompound remove(String key) {
        nbtMap.remove(key);
        return this;
    }

    public INbtTag<?> get(String key) {
        return nbtMap.get(key);
    }

    public INbtTranslator nbtTranslator() {
        return nbtTranslator;
    }

    public AbstractNbtTagCompound setNbtTranslator(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        return this;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        value().forEach((key, val) -> {
            jsonObject.add(key, val.toJson());
        });
        return jsonObject;
    }

    public Map<String, Object> unwarppedMap() {
        Map<String, Object> map = new HashMap<>();
        nbtMap.forEach((key, nbtTag) -> {
            if (nbtTag instanceof AbstractNbtTagCompound) {
                map.put(key, ((AbstractNbtTagCompound) nbtTag).unwarppedMap());
            } else if(nbtTag instanceof AbstractNbtTagList) {
                map.put(key, ((AbstractNbtTagList) nbtTag).unwrappedList());
            }  else if (nbtTag instanceof INumberNbt) {
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

        AbstractNbtTagCompound that = (AbstractNbtTagCompound) object;

        return Objects.equals(nbtMap, that.nbtMap);
    }

    @Override
    public int hashCode() {
        return nbtMap != null ? nbtMap.hashCode() : 0;
    }

}
