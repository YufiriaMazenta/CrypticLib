package crypticlib.nms.nbt;

import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractNbtCompound implements INbtTag<Map<String, INbtTag<?>>>, INbtContainer<Map<String, Object>> {

    private INbtTranslator nbtTranslator;
    private Map<String, INbtTag<?>> nbtMap;

    public AbstractNbtCompound(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        this.nbtMap = new ConcurrentHashMap<>();
    }

    public AbstractNbtCompound(Object nmsNbtCompound, INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        this.nbtMap = new ConcurrentHashMap<>();
        fromNms(nmsNbtCompound);
    }

    public AbstractNbtCompound(Map<String, Object> nbtMap, INbtTranslator nbtTranslator) {
        this.nbtMap = new ConcurrentHashMap<>();
        this.nbtTranslator = nbtTranslator;
        nbtMap.forEach((key, val) -> {
            this.nbtMap.put(key, getNbtTranslator().fromObject(val));
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

    public boolean set(String key, INbtTag<?> nbt) {
        return nbtMap.put(key, nbt) != null;
    }

    public INbtTag<?> get(String key) {
        return nbtMap.get(key);
    }

    public INbtTranslator getNbtTranslator() {
        return nbtTranslator;
    }

    public void setNbtTranslator(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
    }

    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        value().forEach((key, val) -> {
            jsonObject.add(key, val.toJson());
        });
        return jsonObject;
    }

    @Override
    public Map<String, Object> unwrappedValue() {
        Map<String, Object> map = new HashMap<>();
        nbtMap.forEach((key, val) -> {
            if (val instanceof INbtContainer) {
                map.put(key, ((INbtContainer<?>) val).unwrappedValue());
            } else if (val instanceof IFormatNbtNumber) {
                map.put(key, ((IFormatNbtNumber) val).formatString());
            } else {
                map.put(key, val.value());
            }
        });
        return map;
    }

}
