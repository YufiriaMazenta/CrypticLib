package crypticlib.util;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yaml配置文件相关工具类
 */
public class YamlConfigHelper {

    /**
     * 将yaml config转化为map
     *
     * @param configSection 原始yaml config
     * @return 转化的map
     */
    public static Map<String, Object> configSection2Map(ConfigurationSection configSection) {
        Map<String, Object> map = new HashMap<>();
        configSection.getValues(false).forEach(
            (key, value) -> {
                if (value instanceof ConfigurationSection) {
                    map.put(key, configSection2Map((ConfigurationSection) value));
                } else if (configSection instanceof List) {
                    map.put(key, configList2List((List<?>) value));
                } else {
                    map.put(key, value);
                }
            }
        );
        return map;
    }

    /**
     * 将yaml config列表转化为基础数据类型列表
     *
     * @param origin 原始yaml config
     * @return 转化的列表
     */
    public static List<Object> configList2List(List<?> origin) {
        List<Object> list = new ArrayList<>();
        for (Object o : origin) {
            if (o instanceof ConfigurationSection) {
                list.add(configSection2Map((ConfigurationSection) o));
            } else if (o instanceof List<?>) {
                list.add(configList2List((List<?>) o));
            } else {
                list.add(o);
            }
        }
        return list;
    }

}
