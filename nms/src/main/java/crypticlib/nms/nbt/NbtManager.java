package crypticlib.nms.nbt;

import com.google.gson.JsonObject;
import crypticlib.CrypticLib;
import crypticlib.nms.nbt.v1_13_R1.V1_13_R1NbtCompound;
import crypticlib.nms.nbt.v1_13_R2.V1_13_R2NbtCompound;
import crypticlib.nms.nbt.v1_14_R1.V1_14_R1NbtCompound;
import crypticlib.nms.nbt.v1_15_R1.V1_15_R1NbtCompound;
import crypticlib.nms.nbt.v1_16_R1.V1_16_R1NbtCompound;
import crypticlib.nms.nbt.v1_16_R2.V1_16_R2NbtCompound;
import crypticlib.nms.nbt.v1_16_R3.V1_16_R3NbtCompound;
import crypticlib.nms.nbt.v1_17_R1.V1_17_R1NbtCompound;
import crypticlib.nms.nbt.v1_18_R1.V1_18_R1NbtCompound;
import crypticlib.nms.nbt.v1_18_R2.V1_18_R2NbtCompound;
import crypticlib.nms.nbt.v1_19_R1.V1_19_R1NbtCompound;
import crypticlib.nms.nbt.v1_19_R2.V1_19_R2NbtCompound;
import crypticlib.nms.nbt.v1_19_R3.V1_19_R3NbtCompound;
import crypticlib.nms.nbt.v1_20_R1.V1_20_R1NbtCompound;
import crypticlib.nms.nbt.v1_20_R2.V1_20_R2NbtCompound;
import crypticlib.util.JsonUtil;
import crypticlib.util.YamlConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class NbtManager {

    private static final Map<String, Function<Map<String, Object>, AbstractNbtCompound>> nbtCompoundProviderMap;

    static {
        nbtCompoundProviderMap = new ConcurrentHashMap<>();
        nbtCompoundProviderMap.put("v1_13_R1", V1_13_R1NbtCompound::new);
        nbtCompoundProviderMap.put("v1_13_R2", V1_13_R2NbtCompound::new);
        nbtCompoundProviderMap.put("v1_14_R1", V1_14_R1NbtCompound::new);
        nbtCompoundProviderMap.put("v1_15_R1", V1_15_R1NbtCompound::new);
        nbtCompoundProviderMap.put("v1_16_R1", V1_16_R1NbtCompound::new);
        nbtCompoundProviderMap.put("v1_16_R2", V1_16_R2NbtCompound::new);
        nbtCompoundProviderMap.put("v1_16_R3", V1_16_R3NbtCompound::new);
        nbtCompoundProviderMap.put("v1_17_R1", V1_17_R1NbtCompound::new);
        nbtCompoundProviderMap.put("v1_18_R1", V1_18_R1NbtCompound::new);
        nbtCompoundProviderMap.put("v1_18_R2", V1_18_R2NbtCompound::new);
        nbtCompoundProviderMap.put("v1_19_R1", V1_19_R1NbtCompound::new);
        nbtCompoundProviderMap.put("v1_19_R2", V1_19_R2NbtCompound::new);
        nbtCompoundProviderMap.put("v1_19_R3", V1_19_R3NbtCompound::new);
        nbtCompoundProviderMap.put("v1_20_R1", V1_20_R1NbtCompound::new);
        nbtCompoundProviderMap.put("v1_20_R2", V1_20_R2NbtCompound::new);
    }

    public static AbstractNbtCompound map2NbtCompound(Map<String, Object> map) {
        return nbtCompoundProviderMap.getOrDefault(CrypticLib.nmsVersion(), (map1) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(map);
    }

    public static AbstractNbtCompound config2NbtCompound(ConfigurationSection config) {
        return map2NbtCompound(YamlConfigUtil.configSection2Map(config));
    }

    public static AbstractNbtCompound json2NbtCompound(JsonObject jsonObject) {
        return map2NbtCompound(JsonUtil.json2Map(jsonObject));
    }

}
