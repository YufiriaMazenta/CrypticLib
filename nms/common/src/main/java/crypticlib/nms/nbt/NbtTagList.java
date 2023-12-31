package crypticlib.nms.nbt;

import com.google.gson.JsonArray;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public abstract class NbtTagList implements INbtTag<List<Object>> {

    protected List<INbtTag<?>> nbtList = new CopyOnWriteArrayList<>();
    protected INbtTranslator nbtTranslator;

    public NbtTagList(List<Object> nbtList, INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        for (Object object : nbtList) {
            this.nbtList.add(nbtTranslator().translateObject(object));
        }
    }

    public NbtTagList(Object nmsNbtList, INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        fromNms(nmsNbtList);
    }

    public NbtTagList(INbtTranslator nbtTranslator) {
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

    /**
     * 将另外一个NbtTagList的内容全部添加到此NbtTagList
     *
     * @param nbtTagList 另外一个NbtTagList
     */
    public NbtTagList addAll(NbtTagList nbtTagList) {
        this.nbtList.addAll(nbtTagList.nbtList);
        return this;
    }

    @Override
    public @NotNull NbtType type() {
        return NbtType.LIST;
    }


    public @NotNull List<INbtTag<?>> nbtList() {
        return nbtList;
    }

    public NbtTagList setNbtList(@NotNull List<INbtTag<?>> value) {
        this.nbtList.clear();
        this.nbtList.addAll(value);
        return this;
    }

    @Override
    public @NotNull List<Object> value() {
        List<Object> list = new ArrayList<>();
        for (INbtTag<?> nbtTag : nbtList) {
            if (nbtTag instanceof INumberNbt) {
                list.add(((INumberNbt) nbtTag).formatValue());
            } else {
                list.add(nbtTag.value());
            }
        }
        return list;
    }

    @Override
    public NbtTagList setValue(@NotNull List<Object> value) {
        this.nbtList.clear();
        value.forEach(val -> this.nbtList.add(nbtTranslator.translateObject(val)));
        return this;
    }

    public INbtTranslator nbtTranslator() {
        return nbtTranslator;
    }

    public NbtTagList setNbtTranslator(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
        return this;
    }

    @Override
    public @NotNull JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (INbtTag<?> nbtTag : nbtList) {
            jsonArray.add(nbtTag.toJson());
        }
        return jsonArray;
    }

    @Override
    public String toString() {
        return toNms().toString();
    }

}
