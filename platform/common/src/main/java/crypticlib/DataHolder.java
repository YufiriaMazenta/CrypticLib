package crypticlib;

import java.util.Map;
import java.util.Optional;

public interface DataHolder {

    /**
     * 获取当前存储的所有数据
     * @return
     */
    Map<String, Object> allData();

    /**
     * 设置当前存储的所有数据
     * @param data
     */
    void setAllData(Map<String, Object> data);

    /**
     * 获取某个键存储的数据
     * @param key
     * @return
     */
    Optional<Object> getData(String key);

    /**
     * 记录一个数据
     * @param key
     * @param value
     * @return 如果原本存在数据,返回被覆盖的数据
     */
    Object putData(String key, Object value);

    void clearData();

}
