package crypticlib.util;

import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Yaml配置文件相关工具类
 */
public class YamlConfigUtil {

    /**
     * 将yaml config转化为map
     *
     * @param configSection 原始yaml config
     * @return 转化的map
     */
    public static Map<String, Object> configSection2Map(ConfigurationSection configSection) {
        Map<String, Object> map = new HashMap<>();
        for (String key : configSection.getKeys(false)) {
            if (configSection.isConfigurationSection(key)) {
                map.put(key, configSection2Map(Objects.requireNonNull(configSection.getConfigurationSection(key))));
            } else if (configSection.isList(key)) {
                map.put(key, configList2List(Objects.requireNonNull(configSection.getList(key))));
            } else {
                map.put(key, configSection.get(key));
            }
        }
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
