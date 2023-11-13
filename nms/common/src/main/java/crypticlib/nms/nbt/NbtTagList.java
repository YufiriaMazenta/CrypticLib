package crypticlib.nms.nbt;

import com.google.gson.JsonArray;
import crypticlib.util.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class NbtTagList implements INbtTag<List<INbtTag<?>>> {

    private List<INbtTag<?>> nbtList;
    private INbtTranslator nbtTranslator;

    public NbtTagList(List<Object> nbtList, INbtTranslator nbtTranslator) {
        this.nbtList = new ArrayList<>();
        this.nbtTranslator = nbtTranslator;
        for (Object object : nbtList) {
            this.nbtList.add(nbtTranslator().translateObject(object));
        }
    }

    public NbtTagList(Object nmsNbtList, INbtTranslator nbtTranslator) {
        this.nbtList = new ArrayList<>();
        this.nbtTranslator = nbtTranslator;
        fromNms(nmsNbtList);
    }

    public NbtTagList(INbtTranslator nbtTranslator) {
        this.nbtList = new ArrayList<>();
        this.nbtTranslator = nbtTranslator;
    }

    public INbtTag<?> get(int index) {
        return nbtList.get(index);
    }

    public NbtTagList set(int index, INbtTag<?> nbt) {
        nbtList.set(index, nbt);
        return this;
    }

    public NbtTagList set(int index, Byte value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, byte[] value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, Double value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, Float value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, Integer value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, int[] value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, Long value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, long[] value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, Short value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, String value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, Map<String, Object> value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList set(int index, List<Object> value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(INbtTag<?> nbt) {
        nbtList.add(nbt);
        return this;
    }

    public NbtTagList add(Byte value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(byte[] value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(Double value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(Float value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(Integer value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(int[] value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(Long value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(long[] value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(Short value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(String value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(Map<String, Object> value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList add(List<Object> value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public NbtTagList remove(int index) {
        nbtList.remove(index);
        return this;
    }

    public NbtTagList clear() {
        nbtList.clear();
        return this;
    }

    @Override
    public NbtType type() {
        return NbtType.LIST;
    }

    @Override
    public List<INbtTag<?>> value() {
        return nbtList;
    }

    @Override
    public void setValue(List<INbtTag<?>> value) {
        this.nbtList = value;
    }

    public INbtTranslator nbtTranslator() {
        return nbtTranslator;
    }

    public NbtTagList setNbtTranslator(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        return this;
    }

    @Override
    public JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (INbtTag<?> nbtTag : value()) {
            jsonArray.add(nbtTag.toJson());
        }
        return jsonArray;
    }

    public List<Object> unwrappedList() {
        List<Object> list = new ArrayList<>();
        for (INbtTag<?> nbtTag : value()) {
            if (nbtTag instanceof NbtTagCompound) {
                list.add(((NbtTagCompound) nbtTag).unwarppedMap());
            } else if(nbtTag instanceof NbtTagList) {
                list.add(((NbtTagList) nbtTag).unwrappedList());
            } else if (nbtTag instanceof INumberNbt) {
                list.add(((INumberNbt) nbtTag).format());
            } else {
                list.add(nbtTag.value());
            }
        }
        return list;
    }

    @Override
    public String toString() {
        return JsonUtil.json2Str(toJson());
    }

}
