package crypticlib.util;

import com.google.common.base.Charsets;
import crypticlib.BukkitPlugin;
import crypticlib.CrypticLib;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemoryConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Yaml配置文件相关工具类
 */
public class BukkitConfigHelper {

    /**
     * 获取打包在插件jar内的文件内容
     * @param configFilePath 要获取的语言
     * @throws RuntimeException 如果出现IO异常，将会抛出错误
     * @return 解析完毕的config
     */
    public static YamlConfiguration getBuiltinConfig(String configFilePath) {
        try(InputStream fileIS = IOHelper.getBuiltinResource(configFilePath)) {
            if (fileIS == null)
                return null;
            return YamlConfiguration.loadConfiguration(new InputStreamReader(fileIS, Charsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
                } else if (value instanceof List) {
                    map.put(key, configList2List((List<?>) value));
                } else {
                    map.put(key, value);
                }
            }
        );
        return map;
    }

    public static @NotNull ConfigurationSection map2ConfigSection(Map<?, ?> map) {
        MemoryConfiguration memoryConfigSection = new MemoryConfiguration();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String key = String.valueOf(e.getKey());
            Object value = e.getValue();
            if (value instanceof Map<?, ?>) {
                memoryConfigSection.set(key, map2ConfigSection((Map<?, ?>) value));
            } else {
                memoryConfigSection.set(key, value);
            }
        }
        return memoryConfigSection;
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
