package crypticlib.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;

public class JsonUtil {

    private static final Gson gson = new Gson();

    /**
     * 将json对象转化为字符串
     * @param json json对象
     * @return 转化的字符串
     */
    public static String json2Str(JsonElement json) {
        return gson.toJson(json);
    }

    /**
     * 将字符串解析为Json对象
     * @param str 原始字符串
     * @return 解析完毕的Json对象
     */
    public static JsonObject str2JsonObject(String str) {
        return gson.fromJson(str, JsonObject.class);
    }

    /**
     * 将字符串解析为Json数组
     * @param str 原始字符串
     * @return 解析完毕的Json数组
     */
    public static JsonArray str2JsonArray(String str) {
        return gson.fromJson(str, JsonArray.class);
    }

    /**
     * 将yaml config转化为json对象
     * 需要注意,yaml config本身对于数字类型的保存有误差
     * @param configurationSection yaml的config section
     * @return 转化完毕的json对象
     */
    public static JsonObject configSection2Json(ConfigurationSection configurationSection) {
        return gson.fromJson(gson.toJson(YamlConfigUtil.configSection2Map(configurationSection)), JsonObject.class);
    }

    /**
     * 将yaml config中读取到的list转化为json数组
     * @param list 原始数组
     * @return 转化完毕的json数组
     */
    public static JsonArray configList2JsonArray(List<?> list) {
        return gson.fromJson(gson.toJson(YamlConfigUtil.configList2List(list)), JsonArray.class);
    }

    /**
     * 将json转化为map
     * @param jsonObject 原始json对象
     * @return 转化完毕的map
     */
    public static Map<String, Object> json2Map(JsonObject jsonObject) {
        return gson.fromJson(jsonObject, Map.class);
    }

    /**
     * 将json字符串转化为map
     * @param jsonStr 原始json字符串
     * @return 转化完毕的map
     */
    public static Map<String, Object> json2Map(String jsonStr) {
        return json2Map(str2JsonObject(jsonStr));
    }

    /**
     * 获取Gson对象
     * @return gson对象
     */
    public static Gson getGson() {
        return gson;
    }

}
