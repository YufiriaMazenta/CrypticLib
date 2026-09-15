package crypticlib.util;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Map;

/**
 * Json相关工具类
 */
public class JsonHelper {

    private static final Gson gson = new Gson();

    /**
     * 将json对象转化为字符串
     *
     * @param json json对象
     * @return 转化的字符串
     */
    public static String json2Str(JsonElement json) {
        return gson.toJson(json);
    }

    /**
     * 将字符串解析为Json对象
     *
     * @param str 原始字符串
     * @return 解析完毕的Json对象
     */
    public static JsonObject str2JsonObject(String str) {
        return gson.fromJson(str, JsonObject.class);
    }

    /**
     * 将字符串解析为Json数组
     *
     * @param str 原始字符串
     * @return 解析完毕的Json数组
     */
    public static JsonArray str2JsonArray(String str) {
        return gson.fromJson(str, JsonArray.class);
    }

    /**
     * 将json转化为map
     *
     * @param jsonObject 原始json对象
     * @return 转化完毕的map
     */
    public static Map<String, Object> json2Map(JsonObject jsonObject) {
        return gson.fromJson(jsonObject, Map.class);
    }

    /**
     * 将json字符串转化为map
     *
     * @param jsonStr 原始json字符串
     * @return 转化完毕的map
     */
    public static Map<String, Object> json2Map(String jsonStr) {
        return json2Map(str2JsonObject(jsonStr));
    }

    /**
     * 获取Gson对象
     *
     * @return gson对象
     */
    public static Gson getGson() {
        return gson;
    }

}
