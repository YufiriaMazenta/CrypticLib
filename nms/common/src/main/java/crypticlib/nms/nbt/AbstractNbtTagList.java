package crypticlib.nms.nbt;

import com.google.gson.JsonArray;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNbtTagList implements INbtTag<List<INbtTag<?>>>, INbtContainer<List<Object>> {

    private List<INbtTag<?>> nbtList;
    private INbtTranslator nbtTranslator;

    public AbstractNbtTagList(List<Object> nbtList, INbtTranslator nbtTranslator) {
        this.nbtList = new ArrayList<>();
        this.nbtTranslator = nbtTranslator;
        for (Object object : nbtList) {
            this.nbtList.add(getNbtTranslator().fromObject(object));
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

    public boolean set(int index, INbtTag<?> nbt) {
        return nbtList.set(index, nbt) != null;
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

    public INbtTranslator getNbtTranslator() {
        return nbtTranslator;
    }

    public void setNbtTranslator(INbtTranslator nbtTranslator) {
        this.nbtTranslator = nbtTranslator;
    }

    @Override
    public JsonArray toJson() {
        JsonArray jsonArray = new JsonArray();
        for (INbtTag<?> nbtTag : value()) {
            jsonArray.add(nbtTag.toJson());
        }
        return jsonArray;
    }

    @Override
    public List<Object> unwrappedValue() {
        List<Object> list = new ArrayList<>();
        for (INbtTag<?> nbtTag : value()) {
            if (nbtTag instanceof INbtContainer) {
                list.add(((INbtContainer<?>) nbtTag).unwrappedValue());
            } else if (nbtTag instanceof IFormatNbtNumber) {
                list.add(((IFormatNbtNumber) nbtTag).formatString());
            } else {
                list.add(nbtTag.value());
            }
        }
        return list;
    }

}
