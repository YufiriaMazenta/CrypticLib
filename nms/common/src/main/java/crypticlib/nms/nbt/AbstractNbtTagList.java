package crypticlib.nms.nbt;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractNbtTagList implements INbtTag<List<INbtTag<?>>> {

    private List<INbtTag<?>> nbtList;
    private INbtTranslator nbtTranslator;

    public AbstractNbtTagList(List<Object> nbtList, INbtTranslator nbtTranslator) {
        this.nbtList = new ArrayList<>();
        this.nbtTranslator = nbtTranslator;
        for (Object object : nbtList) {
            this.nbtList.add(nbtTranslator().translateObject(object));
        }
    }

    public AbstractNbtTagList(Object nmsNbtList, INbtTranslator nbtTranslator) {
        this.nbtList = new ArrayList<>();
        this.nbtTranslator = nbtTranslator;
        fromNms(nmsNbtList);
    }

    public AbstractNbtTagList(INbtTranslator nbtTranslator) {
        this.nbtList = new ArrayList<>();
        this.nbtTranslator = nbtTranslator;
    }

    public INbtTag<?> get(int index) {
        return nbtList.get(index);
    }

    public AbstractNbtTagList set(int index, INbtTag<?> nbt) {
        nbtList.set(index, nbt);
        return this;
    }

    public AbstractNbtTagList set(int index, Byte value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, byte[] value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, Double value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, Float value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, Integer value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, int[] value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, Long value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, long[] value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, Short value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, String value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, Map<String, Object> value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList set(int index, List<Object> value) {
        nbtList.set(index, nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList remove(int index) {
        nbtList.remove(index);
        return this;
    }

    public AbstractNbtTagList add(INbtTag<?> nbt) {
        nbtList.add(nbt);
        return this;
    }

    public AbstractNbtTagList add(Byte value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(byte[] value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(Double value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(Float value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(Integer value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(int[] value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(Long value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(long[] value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(Short value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(String value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(Map<String, Object> value) {
        nbtList.add(nbtTranslator.translateObject(value));
        return this;
    }

    public AbstractNbtTagList add(List<Object> value) {
        nbtList.add(nbtTranslator.translateObject(value));
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

    public AbstractNbtTagList setNbtTranslator(INbtTranslator nbtTranslator) {
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
            if (nbtTag instanceof AbstractNbtTagCompound) {
                list.add(((AbstractNbtTagCompound) nbtTag).unwarppedMap());
            } else if(nbtTag instanceof AbstractNbtTagList) {
                list.add(((AbstractNbtTagList) nbtTag).unwrappedList());
            } else if (nbtTag instanceof INumberNbt) {
                list.add(((INumberNbt) nbtTag).format());
            } else {
                list.add(nbtTag.value());
            }
        }
        return list;
    }

}
