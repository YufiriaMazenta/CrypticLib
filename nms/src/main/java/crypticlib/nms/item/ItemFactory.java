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

    private static final Map<String, Function<ItemStack, NbtItem>> nbtItemProviderMap1;
    private static final Map<String, BiFunction<Material, NbtTagCompound, NbtItem>> nbtItemProviderMap2;
    private static final Map<String, TernaryFunction<Material, NbtTagCompound, Integer, NbtItem>> nbtItemProviderMap3;

    static {
        nbtItemProviderMap1 = new ConcurrentHashMap<>();
        nbtItemProviderMap2 = new ConcurrentHashMap<>();
        nbtItemProviderMap3 = new ConcurrentHashMap<>();

        regNbtItemProvider("v1_12_R1", V1_12_R1NbtItem::new, V1_12_R1NbtItem::new, V1_12_R1NbtItem::new);
        regNbtItemProvider("v1_13_R1", V1_13_R1NbtItem::new, V1_13_R1NbtItem::new, V1_13_R1NbtItem::new);
        regNbtItemProvider("v1_13_R2", V1_13_R2NbtItem::new, V1_13_R2NbtItem::new, V1_13_R2NbtItem::new);
        regNbtItemProvider("v1_14_R1", V1_14_R1NbtItem::new, V1_14_R1NbtItem::new, V1_14_R1NbtItem::new);
        regNbtItemProvider("v1_15_R1", V1_15_R1NbtItem::new, V1_15_R1NbtItem::new, V1_15_R1NbtItem::new);
        regNbtItemProvider("v1_16_R1", V1_16_R1NbtItem::new, V1_16_R1NbtItem::new, V1_16_R1NbtItem::new);
        regNbtItemProvider("v1_16_R2", V1_16_R2NbtItem::new, V1_16_R2NbtItem::new, V1_16_R2NbtItem::new);
        regNbtItemProvider("v1_16_R3", V1_16_R3NbtItem::new, V1_16_R3NbtItem::new, V1_16_R3NbtItem::new);
        regNbtItemProvider("v1_17_R1", V1_17_R1NbtItem::new, V1_17_R1NbtItem::new, V1_17_R1NbtItem::new);
        regNbtItemProvider("v1_18_R1", V1_18_R1NbtItem::new, V1_18_R1NbtItem::new, V1_18_R1NbtItem::new);
        regNbtItemProvider("v1_18_R2", V1_18_R2NbtItem::new, V1_18_R2NbtItem::new, V1_18_R2NbtItem::new);
        regNbtItemProvider("v1_19_R1", V1_19_R1NbtItem::new, V1_19_R1NbtItem::new, V1_19_R1NbtItem::new);
        regNbtItemProvider("v1_19_R2", V1_19_R2NbtItem::new, V1_19_R2NbtItem::new, V1_19_R2NbtItem::new);
        regNbtItemProvider("v1_19_R3", V1_19_R3NbtItem::new, V1_19_R3NbtItem::new, V1_19_R3NbtItem::new);
        regNbtItemProvider("v1_20_R1", V1_20_R1NbtItem::new, V1_20_R1NbtItem::new, V1_20_R1NbtItem::new);
        regNbtItemProvider("v1_20_R2", V1_20_R2NbtItem::new, V1_20_R2NbtItem::new, V1_20_R2NbtItem::new);
        regNbtItemProvider("v1_20_R3", V1_20_R3NbtItem::new, V1_20_R3NbtItem::new, V1_20_R3NbtItem::new);
    }

    public static NbtItem item(ItemStack itemStack) {
        return nbtItemProviderMap1.getOrDefault(CrypticLib.nmsVersion(), i -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(itemStack);
    }

    public static NbtItem item(Material material) {
        return item(material, NbtFactory.emptyNbtCompound());
    }

    public static NbtItem item(Material material, Integer amount) {
        return item(material, NbtFactory.emptyNbtCompound(), amount);
    }

    public static NbtItem item(Material material, NbtTagCompound nbtTagCompound) {
        return nbtItemProviderMap2.getOrDefault(CrypticLib.nmsVersion(), (material_, nbtCompound_) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(material, nbtTagCompound);
    }

    public static NbtItem item(Material material, NbtTagCompound nbtTagCompound, Integer amount) {
        return nbtItemProviderMap3.getOrDefault(CrypticLib.nmsVersion(), (material_, nbtTagCompound_, amount_) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(material, nbtTagCompound, amount);
    }

    public static NbtItem item(ConfigurationSection config) {
        String materialStr = config.getString("material", "air");
        Material material = Material.matchMaterial(materialStr);
        NbtTagCompound nbtCompound = NbtFactory.emptyNbtCompound();
        int amount = config.getInt("amount", 1);
        if (config.isConfigurationSection("nbt")) {
            nbtCompound = NbtFactory.config2NbtTagCompound(config.getConfigurationSection("nbt"));
        }
        return item(material, nbtCompound, amount);
    }

    public static void regNbtItemProvider(String nmsVersion, Function<ItemStack, NbtItem> provider1, BiFunction<Material, NbtTagCompound, NbtItem> provider2, TernaryFunction<Material, NbtTagCompound, Integer, NbtItem> provider3) {
        nbtItemProviderMap1.put(nmsVersion, provider1);
        nbtItemProviderMap2.put(nmsVersion, provider2);
        nbtItemProviderMap3.put(nmsVersion, provider3);
    }

}
