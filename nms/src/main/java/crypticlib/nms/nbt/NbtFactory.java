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
import crypticlib.util.JsonUtil;
import crypticlib.util.YamlConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * CrypticLib的Nbt提供工厂
 */
public class NbtFactory {

    private static final Map<String, Function<Map<String, Object>, NbtTagCompound>> nbtTagCompoundProviderMap;
    private static final Map<String, Supplier<NbtTagCompound>> emptyNbtTagCompoundProviderMap;

    static {
        nbtTagCompoundProviderMap = new ConcurrentHashMap<>();
        emptyNbtTagCompoundProviderMap = new ConcurrentHashMap<>();

        regNbtTagCompoundProvider("v1_12_R1", V1_12_R1NbtTagCompound::new, V1_12_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_13_R1", V1_13_R1NbtTagCompound::new, V1_13_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_13_R2", V1_13_R2NbtTagCompound::new, V1_13_R2NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_14_R1", V1_14_R1NbtTagCompound::new, V1_14_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_15_R1", V1_15_R1NbtTagCompound::new, V1_15_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_16_R1", V1_16_R1NbtTagCompound::new, V1_16_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_16_R2", V1_16_R2NbtTagCompound::new, V1_16_R2NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_16_R3", V1_16_R3NbtTagCompound::new, V1_16_R3NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_17_R1", V1_17_R1NbtTagCompound::new, V1_17_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_18_R1", V1_18_R1NbtTagCompound::new, V1_18_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_18_R2", V1_18_R2NbtTagCompound::new, V1_18_R2NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_19_R1", V1_19_R1NbtTagCompound::new, V1_19_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_19_R2", V1_19_R2NbtTagCompound::new, V1_19_R2NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_19_R3", V1_19_R3NbtTagCompound::new, V1_19_R3NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_20_R1", V1_20_R1NbtTagCompound::new, V1_20_R1NbtTagCompound::new);
        regNbtTagCompoundProvider("v1_20_R2", V1_20_R2NbtTagCompound::new, V1_20_R2NbtTagCompound::new);
    }

    public static NbtTagCompound map2NbtTagCompound(Map<String, Object> map) {
        processMap(map);
        return nbtTagCompoundProviderMap.getOrDefault(CrypticLib.nmsVersion(), (map1) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(map);
    }

    public static NbtTagCompound config2NbtTagCompound(ConfigurationSection config) {
        return map2NbtTagCompound(YamlConfigUtil.configSection2Map(config));
    }

    public static NbtTagCompound json2NbtTagCompound(JsonObject jsonObject) {
        return map2NbtTagCompound(JsonUtil.json2Map(jsonObject));
    }

    public static NbtTagCompound emptyNbtCompound() {
        return emptyNbtTagCompoundProviderMap.getOrDefault(CrypticLib.nmsVersion(), () -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).get();
    }

    public static void regNbtTagCompoundProvider(String nmsVersion, Function<Map<String, Object>, NbtTagCompound> nbtTagCompoundProvider, Supplier<NbtTagCompound> emptyNbtTagCompoundProvider) {
        nbtTagCompoundProviderMap.put(nmsVersion, nbtTagCompoundProvider);
        emptyNbtTagCompoundProviderMap.put(nmsVersion, emptyNbtTagCompoundProvider);
    }

    private static void processMap(Map<String, Object> originMap) {
        originMap.forEach((key, value) -> {
            if (value instanceof String) {
                originMap.put(key, processStr((String) value));
            } else if (value instanceof Map) {
                processMap((Map<String, Object>) value);
            } else if (value instanceof List) {
                processList((List<Object>) value);
            }
        });
    }

    private static void processList(List<Object> originList) {
        for (int i = 0; i < originList.size(); i++) {
            Object o = originList.get(i);
            if (o instanceof String) {
                originList.set(i, processStr(o.toString()));
            } else if (o instanceof Map) {
                processMap((Map<String, Object>) o);
            } else if (o instanceof List) {
                processList((List<Object>) o);
            }
        }
    }

    private static Object processStr(String originStr) {
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
