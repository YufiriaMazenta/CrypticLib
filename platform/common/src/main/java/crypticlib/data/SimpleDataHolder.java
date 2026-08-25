package crypticlib.data;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DataHolder 的通用实现，供需要 DataHolder 功能的类组合使用
 */
public class SimpleDataHolder implements DataHolder {

    private final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    @Override
    public Map<String, Object> allData() {
        return dataMap;
    }

    @Override
    public void setAllData(Map<String, Object> data) {
        this.dataMap.clear();
        this.dataMap.putAll(data);
    }

    @Override
    public Optional<Object> getData(String key) {
        if (dataMap.containsKey(key)) {
            return Optional.ofNullable(dataMap.get(key));
        }
        return Optional.empty();
    }

    @Override
    public Object putData(String key, Object value) {
        return dataMap.put(key, value);
    }

    @Override
    public void clearData() {
        dataMap.clear();
    }

}
