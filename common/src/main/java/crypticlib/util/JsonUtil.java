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

    public static String json2Str(JsonElement json) {
        return gson.toJson(json);
    }

    public static JsonObject str2JsonObject(String str) {
        return gson.fromJson(str, JsonObject.class);
    }

    public static JsonArray str2JsonArray(String str) {
        return gson.fromJson(str, JsonArray.class);
    }

    public static JsonObject configSection2Json(ConfigurationSection configurationSection) {
        return gson.fromJson(gson.toJson(YamlConfigUtil.configSection2Map(configurationSection)), JsonObject.class);
    }

    public static JsonArray configList2JsonArray(List<?> list) {
        return gson.fromJson(gson.toJson(YamlConfigUtil.configList2List(list)), JsonArray.class);
    }

    public static Map<String, Object> json2Map(JsonObject jsonObject) {
        return gson.fromJson(jsonObject, Map.class);
    }

    public static Map<String, Object> json2Map(String jsonStr) {
        return json2Map(str2JsonObject(jsonStr));
    }

    public static Gson getGson() {
        return gson;
    }

}
