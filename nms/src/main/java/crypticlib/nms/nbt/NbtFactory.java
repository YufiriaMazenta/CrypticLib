package crypticlib.nms.nbt;

import com.google.gson.JsonObject;
import crypticlib.CrypticLib;
import crypticlib.nms.nbt.v1_12_R1.V1_12_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_13_R1.V1_13_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_13_R2.V1_13_R2NbtTagCompound;
import crypticlib.nms.nbt.v1_14_R1.V1_14_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_15_R1.V1_15_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_16_R1.V1_16_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_16_R2.V1_16_R2NbtTagCompound;
import crypticlib.nms.nbt.v1_16_R3.V1_16_R3NbtTagCompound;
import crypticlib.nms.nbt.v1_17_R1.V1_17_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_18_R1.V1_18_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_18_R2.V1_18_R2NbtTagCompound;
import crypticlib.nms.nbt.v1_19_R1.V1_19_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_19_R2.V1_19_R2NbtTagCompound;
import crypticlib.nms.nbt.v1_19_R3.V1_19_R3NbtTagCompound;
import crypticlib.nms.nbt.v1_20_R1.V1_20_R1NbtTagCompound;
import crypticlib.nms.nbt.v1_20_R2.V1_20_R2NbtTagCompound;
import crypticlib.nms.nbt.v1_20_R3.V1_20_R3NbtTagCompound;
import crypticlib.util.JsonUtil;
import crypticlib.util.YamlConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CrypticLib的Nbt提供工厂
 */
public class NbtFactory {

    private static final Map<Integer, Function<Map<String, Object>, NbtTagCompound>> nbtTagCompoundProviderMap;
    private static final Map<Integer, Supplier<NbtTagCompound>> emptyNbtTagCompoundProviderMap;
    private static final Map<Integer, Function<String, NbtTagCompound>> mojangsonToNbtCompoundProviderMap;

    static {
        nbtTagCompoundProviderMap = new ConcurrentHashMap<>();
        emptyNbtTagCompoundProviderMap = new ConcurrentHashMap<>();
        mojangsonToNbtCompoundProviderMap = new ConcurrentHashMap<>();

        regNbtTagCompoundProvider(11200, V1_12_R1NbtTagCompound::new, V1_12_R1NbtTagCompound::new, V1_12_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11201, V1_12_R1NbtTagCompound::new, V1_12_R1NbtTagCompound::new, V1_12_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11202, V1_12_R1NbtTagCompound::new, V1_12_R1NbtTagCompound::new, V1_12_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11300, V1_13_R1NbtTagCompound::new, V1_13_R1NbtTagCompound::new, V1_13_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11301, V1_13_R1NbtTagCompound::new, V1_13_R1NbtTagCompound::new, V1_13_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11302, V1_13_R2NbtTagCompound::new, V1_13_R2NbtTagCompound::new, V1_13_R2NbtTagCompound::new);
        regNbtTagCompoundProvider(11400, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11401, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11402, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11403, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11404, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11500, V1_15_R1NbtTagCompound::new, V1_15_R1NbtTagCompound::new, V1_15_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11501, V1_15_R1NbtTagCompound::new, V1_15_R1NbtTagCompound::new, V1_15_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11502, V1_15_R1NbtTagCompound::new, V1_15_R1NbtTagCompound::new, V1_15_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11600, V1_16_R1NbtTagCompound::new, V1_16_R1NbtTagCompound::new, V1_16_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11601, V1_16_R1NbtTagCompound::new, V1_16_R1NbtTagCompound::new, V1_16_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11602, V1_16_R1NbtTagCompound::new, V1_16_R1NbtTagCompound::new, V1_16_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11603, V1_16_R2NbtTagCompound::new, V1_16_R2NbtTagCompound::new, V1_16_R2NbtTagCompound::new);
        regNbtTagCompoundProvider(11604, V1_16_R3NbtTagCompound::new, V1_16_R3NbtTagCompound::new, V1_16_R3NbtTagCompound::new);
        regNbtTagCompoundProvider(11605, V1_16_R3NbtTagCompound::new, V1_16_R3NbtTagCompound::new, V1_16_R3NbtTagCompound::new);
        regNbtTagCompoundProvider(11700, V1_17_R1NbtTagCompound::new, V1_17_R1NbtTagCompound::new, V1_17_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11701, V1_17_R1NbtTagCompound::new, V1_17_R1NbtTagCompound::new, V1_17_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11800, V1_18_R1NbtTagCompound::new, V1_18_R1NbtTagCompound::new, V1_18_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11801, V1_18_R1NbtTagCompound::new, V1_18_R1NbtTagCompound::new, V1_18_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11802, V1_18_R2NbtTagCompound::new, V1_18_R2NbtTagCompound::new, V1_18_R2NbtTagCompound::new);
        regNbtTagCompoundProvider(11900, V1_19_R1NbtTagCompound::new, V1_19_R1NbtTagCompound::new, V1_19_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11901, V1_19_R1NbtTagCompound::new, V1_19_R1NbtTagCompound::new, V1_19_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(11902, V1_19_R2NbtTagCompound::new, V1_19_R2NbtTagCompound::new, V1_19_R2NbtTagCompound::new);
        regNbtTagCompoundProvider(11903, V1_19_R3NbtTagCompound::new, V1_19_R3NbtTagCompound::new, V1_19_R3NbtTagCompound::new);
        regNbtTagCompoundProvider(11904, V1_19_R3NbtTagCompound::new, V1_19_R3NbtTagCompound::new, V1_19_R3NbtTagCompound::new);
        regNbtTagCompoundProvider(12000, V1_20_R1NbtTagCompound::new, V1_20_R1NbtTagCompound::new, V1_20_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(12001, V1_20_R1NbtTagCompound::new, V1_20_R1NbtTagCompound::new, V1_20_R1NbtTagCompound::new);
        regNbtTagCompoundProvider(12002, V1_20_R2NbtTagCompound::new, V1_20_R2NbtTagCompound::new, V1_20_R2NbtTagCompound::new);
        regNbtTagCompoundProvider(12003, V1_20_R3NbtTagCompound::new, V1_20_R3NbtTagCompound::new, V1_20_R3NbtTagCompound::new);
        regNbtTagCompoundProvider(12004, V1_20_R3NbtTagCompound::new, V1_20_R3NbtTagCompound::new, V1_20_R3NbtTagCompound::new);
    }

    public static NbtTagCompound parseMap(Map<String, Object> map) {
        Map<String, Object> parsedMap = preparseMap(map);
        return nbtTagCompoundProviderMap.getOrDefault(CrypticLib.minecraftVersion(), (map1) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.minecraftVersion());
        }).apply(parsedMap);
    }

    public static NbtTagCompound parseConfig(ConfigurationSection config) {
        return parseMap(YamlConfigUtil.configSection2Map(config));
    }

    public static NbtTagCompound parseJson(JsonObject jsonObject) {
        return parseMap(JsonUtil.json2Map(jsonObject));
    }

    public static NbtTagCompound parseMojangson(String mojangson) {
        return mojangsonToNbtCompoundProviderMap.getOrDefault(CrypticLib.minecraftVersion(), (str) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.minecraftVersion());
        }).apply(mojangson);
    }

    public static NbtTagCompound emptyNbtCompound() {
        return emptyNbtTagCompoundProviderMap.getOrDefault(CrypticLib.minecraftVersion(), () -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.minecraftVersion());
        }).get();
    }

    public static void regNbtTagCompoundProvider(
        Integer minecraftVersion,
        Function<Map<String, Object>, NbtTagCompound> nbtTagCompoundProvider,
        Supplier<NbtTagCompound> emptyNbtTagCompoundProvider,
        Function<String, NbtTagCompound> mojangsonToNbtTagCompoundProvider
    ) {
        nbtTagCompoundProviderMap.put(minecraftVersion, nbtTagCompoundProvider);
        emptyNbtTagCompoundProviderMap.put(minecraftVersion, emptyNbtTagCompoundProvider);
        mojangsonToNbtCompoundProviderMap.put(minecraftVersion, mojangsonToNbtTagCompoundProvider);
    }

    private static Map<String, Object> preparseMap(Map<String, Object> originMap) {
        Map<String, Object> parsedMap = new HashMap<>();
        originMap.forEach((key, value) -> {
            if (value instanceof String) {
                parsedMap.put(key, preparseStr(value.toString()));
            } else if (value instanceof Map) {
                parsedMap.put(key, preparseMap((Map<String, Object>) value));
            } else if (value instanceof List) {
                parsedMap.put(key, preparseList((List<Object>) value));
            } else {
                parsedMap.put(key, value);
            }
        });
        return parsedMap;
    }

    private static List<Object> preparseList(List<Object> originList) {
        List<Object> parsedList = new ArrayList<>(originList.size());
        for (Object object : originList) {
            if (object instanceof String) {
                parsedList.add(preparseStr(object.toString()));
            } else if (object instanceof Map) {
                parsedList.add(preparseMap((Map<String, Object>) object));
            } else if (object instanceof List) {
                parsedList.add(preparseList((List<Object>) object));
            } else {
                parsedList.add(object);
            }
        }
        return parsedList;
    }

    private static Object preparseStr(String originStr) {
        if (originStr.contains("@"))
            return parseNumber(originStr);
        else if (originStr.contains("$"))
            return parseNumberArray(originStr);
        return originStr;
    }

    private static Object parseNumberArray(String originStr) {
        String[] split = originStr.split("\\$");
        if (split.length != 2)
            return originStr;
        String type = split[0];
        String value = split[1];
        value = value.replace("[", "").replace("]", "");
        String[] valueList = value.split(",");
        try {
            switch (type) {
                case "BYTE":
                    byte[] byteArr = new byte[valueList.length];
                    for (int i = 0; i < valueList.length; i++) {
                        byteArr[i] = Byte.parseByte(valueList[i]);
                    }
                    return byteArr;
                case "LONG":
                    long[] longArr = new long[valueList.length];
                    for (int i = 0; i < valueList.length; i++) {
                        longArr[i] = Long.parseLong(valueList[i]);
                    }
                    return longArr;
                case "INT":
                    int[] intArr = new int[valueList.length];
                    for (int i = 0; i < valueList.length; i++) {
                        intArr[i] = Integer.parseInt(valueList[i]);
                    }
                    return intArr;
                default:
                    return originStr;
            }
        } catch (NumberFormatException e) {
            return originStr;
        }
    }

    private static Object parseNumber(String originStr) {
        String[] split = originStr.split("@");
        if (split.length != 2)
            return originStr;
        String type = split[0];
        String value = split[1];
        try {
            switch (type) {
                case "BYTE":
                    return Byte.parseByte(value);
                case "FLOAT":
                    return Float.parseFloat(value);
                case "LONG":
                    return Long.parseLong(value);
                case "SHORT":
                    return Short.parseShort(value);
                case "INT":
                    return Integer.parseInt(value);
                case "DOUBLE":
                    return Double.parseDouble(value);
                default:
                    return originStr;
            }
        } catch (NumberFormatException e) {
            return originStr;
        }
    }

}
