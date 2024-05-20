package crypticlib.nms.item;

import crypticlib.CrypticLib;
import crypticlib.function.TernaryFunction;
import crypticlib.nms.item.v1_12_R1.V1_12_R1NbtItem;
import crypticlib.nms.item.v1_13_R1.V1_13_R1NbtItem;
import crypticlib.nms.item.v1_13_R2.V1_13_R2NbtItem;
import crypticlib.nms.item.v1_14_R1.V1_14_R1NbtItem;
import crypticlib.nms.item.v1_15_R1.V1_15_R1NbtItem;
import crypticlib.nms.item.v1_16_R1.V1_16_R1NbtItem;
import crypticlib.nms.item.v1_16_R2.V1_16_R2NbtItem;
import crypticlib.nms.item.v1_16_R3.V1_16_R3NbtItem;
import crypticlib.nms.item.v1_17_R1.V1_17_R1NbtItem;
import crypticlib.nms.item.v1_18_R1.V1_18_R1NbtItem;
import crypticlib.nms.item.v1_18_R2.V1_18_R2NbtItem;
import crypticlib.nms.item.v1_19_R1.V1_19_R1NbtItem;
import crypticlib.nms.item.v1_19_R2.V1_19_R2NbtItem;
import crypticlib.nms.item.v1_19_R3.V1_19_R3NbtItem;
import crypticlib.nms.item.v1_20_R1.V1_20_R1NbtItem;
import crypticlib.nms.item.v1_20_R2.V1_20_R2NbtItem;
import crypticlib.nms.item.v1_20_R3.V1_20_R3NbtItem;
import crypticlib.nms.nbt.NbtFactory;
import crypticlib.nms.nbt.NbtTagCompound;
import crypticlib.util.MaterialUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * CrypticLib的物品提供工厂
 */
public class ItemFactory {

    private static final Map<Integer, Function<ItemStack, NbtItem>> nbtItemProviderMap1;
    private static final Map<Integer, BiFunction<Material, NbtTagCompound, NbtItem>> nbtItemProviderMap2;
    private static final Map<Integer, TernaryFunction<Material, NbtTagCompound, Integer, NbtItem>> nbtItemProviderMap3;

    static {
        nbtItemProviderMap1 = new ConcurrentHashMap<>();
        nbtItemProviderMap2 = new ConcurrentHashMap<>();
        nbtItemProviderMap3 = new ConcurrentHashMap<>();

        regNbtItemProvider(11200, V1_12_R1NbtItem::new, V1_12_R1NbtItem::new, V1_12_R1NbtItem::new);
        regNbtItemProvider(11201, V1_12_R1NbtItem::new, V1_12_R1NbtItem::new, V1_12_R1NbtItem::new);
        regNbtItemProvider(11202, V1_12_R1NbtItem::new, V1_12_R1NbtItem::new, V1_12_R1NbtItem::new);
        regNbtItemProvider(11300, V1_13_R1NbtItem::new, V1_13_R1NbtItem::new, V1_13_R1NbtItem::new);
        regNbtItemProvider(11301, V1_13_R1NbtItem::new, V1_13_R1NbtItem::new, V1_13_R1NbtItem::new);
        regNbtItemProvider(11302, V1_13_R2NbtItem::new, V1_13_R2NbtItem::new, V1_13_R2NbtItem::new);
        regNbtItemProvider(11400, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new);
        regNbtItemProvider(11401, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new);
        regNbtItemProvider(11402, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new);
        regNbtItemProvider(11403, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new);
        regNbtItemProvider(11404, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new);
        regNbtItemProvider(11500, V1_15_R1NbtItem::new, V1_15_R1NbtItem::new, V1_15_R1NbtItem::new);
        regNbtItemProvider(11501, V1_15_R1NbtItem::new, V1_15_R1NbtItem::new, V1_15_R1NbtItem::new);
        regNbtItemProvider(11502, V1_15_R1NbtItem::new, V1_15_R1NbtItem::new, V1_15_R1NbtItem::new);
        regNbtItemProvider(11600, V1_16_R1NbtItem::new, V1_16_R1NbtItem::new, V1_16_R1NbtItem::new);
        regNbtItemProvider(11601, V1_16_R1NbtItem::new, V1_16_R1NbtItem::new, V1_16_R1NbtItem::new);
        regNbtItemProvider(11602, V1_16_R1NbtItem::new, V1_16_R1NbtItem::new, V1_16_R1NbtItem::new);
        regNbtItemProvider(11603, V1_16_R2NbtItem::new, V1_16_R2NbtItem::new, V1_16_R2NbtItem::new);
        regNbtItemProvider(11604, V1_16_R3NbtItem::new, V1_16_R3NbtItem::new, V1_16_R3NbtItem::new);
        regNbtItemProvider(11605, V1_16_R3NbtItem::new, V1_16_R3NbtItem::new, V1_16_R3NbtItem::new);
        regNbtItemProvider(11700, V1_17_R1NbtItem::new, V1_17_R1NbtItem::new, V1_17_R1NbtItem::new);
        regNbtItemProvider(11701, V1_17_R1NbtItem::new, V1_17_R1NbtItem::new, V1_17_R1NbtItem::new);
        regNbtItemProvider(11800, V1_18_R1NbtItem::new, V1_18_R1NbtItem::new, V1_18_R1NbtItem::new);
        regNbtItemProvider(11801, V1_18_R1NbtItem::new, V1_18_R1NbtItem::new, V1_18_R1NbtItem::new);
        regNbtItemProvider(11802, V1_18_R2NbtItem::new, V1_18_R2NbtItem::new, V1_18_R2NbtItem::new);
        regNbtItemProvider(11900, V1_19_R1NbtItem::new, V1_19_R1NbtItem::new, V1_19_R1NbtItem::new);
        regNbtItemProvider(11901, V1_19_R1NbtItem::new, V1_19_R1NbtItem::new, V1_19_R1NbtItem::new);
        regNbtItemProvider(11902, V1_19_R2NbtItem::new, V1_19_R2NbtItem::new, V1_19_R2NbtItem::new);
        regNbtItemProvider(11903, V1_19_R3NbtItem::new, V1_19_R3NbtItem::new, V1_19_R3NbtItem::new);
        regNbtItemProvider(11904, V1_19_R3NbtItem::new, V1_19_R3NbtItem::new, V1_19_R3NbtItem::new);
        regNbtItemProvider(12000, V1_20_R1NbtItem::new, V1_20_R1NbtItem::new, V1_20_R1NbtItem::new);
        regNbtItemProvider(12001, V1_20_R1NbtItem::new, V1_20_R1NbtItem::new, V1_20_R1NbtItem::new);
        regNbtItemProvider(12002, V1_20_R2NbtItem::new, V1_20_R2NbtItem::new, V1_20_R2NbtItem::new);
        regNbtItemProvider(12003, V1_20_R3NbtItem::new, V1_20_R3NbtItem::new, V1_20_R3NbtItem::new);
    }

    public static NbtItem item(ItemStack itemStack) {
        return nbtItemProviderMap1.getOrDefault(CrypticLib.minecraftVersion(), i -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.minecraftVersion());
        }).apply(itemStack);
    }

    public static NbtItem item(Material material) {
        return item(material, NbtFactory.emptyNbtCompound());
    }

    public static NbtItem item(Material material, Integer amount) {
        return item(material, NbtFactory.emptyNbtCompound(), amount);
    }

    public static NbtItem item(Material material, NbtTagCompound nbtTagCompound) {
        return nbtItemProviderMap2.getOrDefault(CrypticLib.minecraftVersion(), (material_, nbtCompound_) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.minecraftVersion());
        }).apply(material, nbtTagCompound);
    }

    public static NbtItem item(Material material, NbtTagCompound nbtTagCompound, Integer amount) {
        return nbtItemProviderMap3.getOrDefault(CrypticLib.minecraftVersion(), (material_, nbtTagCompound_, amount_) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.minecraftVersion());
        }).apply(material, nbtTagCompound, amount);
    }

    public static NbtItem item(ConfigurationSection config) {
        String materialStr = config.getString("material", "air");
        Material material = MaterialUtil.matchMaterial(materialStr);
        NbtTagCompound nbtCompound = NbtFactory.emptyNbtCompound();
        int amount = config.getInt("amount", 1);
        if (config.isConfigurationSection("nbt")) {
            nbtCompound = NbtFactory.parseConfig(config.getConfigurationSection("nbt"));
        }
        return item(material, nbtCompound, amount);
    }

    public static void regNbtItemProvider(Integer minecraftVersion, Function<ItemStack, NbtItem> provider1, BiFunction<Material, NbtTagCompound, NbtItem> provider2, TernaryFunction<Material, NbtTagCompound, Integer, NbtItem> provider3) {
        nbtItemProviderMap1.put(minecraftVersion, provider1);
        nbtItemProviderMap2.put(minecraftVersion, provider2);
        nbtItemProviderMap3.put(minecraftVersion, provider3);
    }

}
