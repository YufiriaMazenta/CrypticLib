package crypticlib.nms.item;

import crypticlib.CrypticLib;
import crypticlib.nms.item.v1_13_R1.V1_13_R1Item;
import crypticlib.nms.item.v1_13_R2.V1_13_R2Item;
import crypticlib.nms.item.v1_14_R1.V1_14_R1Item;
import crypticlib.nms.item.v1_15_R1.V1_15_R1Item;
import crypticlib.nms.item.v1_16_R1.V1_16_R1Item;
import crypticlib.nms.item.v1_16_R2.V1_16_R2Item;
import crypticlib.nms.item.v1_16_R3.V1_16_R3Item;
import crypticlib.nms.item.v1_17_R1.V1_17_R1Item;
import crypticlib.nms.item.v1_18_R1.V1_18_R1Item;
import crypticlib.nms.item.v1_18_R2.V1_18_R2Item;
import crypticlib.nms.item.v1_19_R1.V1_19_R1Item;
import crypticlib.nms.item.v1_19_R2.V1_19_R2Item;
import crypticlib.nms.item.v1_19_R3.V1_19_R3Item;
import crypticlib.nms.item.v1_20_R1.V1_20_R1Item;
import crypticlib.nms.item.v1_20_R2.V1_20_R2Item;
import crypticlib.nms.nbt.AbstractNbtTagCompound;
import crypticlib.nms.nbt.NbtManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ItemManager {

    private static final Map<String, Function<ItemStack, Item>> itemProviderMap1;
    private static final Map<String, BiFunction<String, AbstractNbtTagCompound, Item>> itemProviderMap2;

    static {
        itemProviderMap1 = new HashMap<>();
        itemProviderMap1.put("v1_13_R1", V1_13_R1Item::new);
        itemProviderMap1.put("v1_13_R2", V1_13_R2Item::new);
        itemProviderMap1.put("v1_14_R1", V1_14_R1Item::new);
        itemProviderMap1.put("v1_15_R1", V1_15_R1Item::new);
        itemProviderMap1.put("v1_16_R1", V1_16_R1Item::new);
        itemProviderMap1.put("v1_16_R2", V1_16_R2Item::new);
        itemProviderMap1.put("v1_16_R3", V1_16_R3Item::new);
        itemProviderMap1.put("v1_17_R1", V1_17_R1Item::new);
        itemProviderMap1.put("v1_18_R1", V1_18_R1Item::new);
        itemProviderMap1.put("v1_18_R2", V1_18_R2Item::new);
        itemProviderMap1.put("v1_19_R1", V1_19_R1Item::new);
        itemProviderMap1.put("v1_19_R2", V1_19_R2Item::new);
        itemProviderMap1.put("v1_19_R3", V1_19_R3Item::new);
        itemProviderMap1.put("v1_20_R1", V1_20_R1Item::new);
        itemProviderMap1.put("v1_20_R2", V1_20_R2Item::new);

        itemProviderMap2 = new HashMap<>();
        itemProviderMap2.put("v1_13_R1", V1_13_R1Item::new);
        itemProviderMap2.put("v1_13_R2", V1_13_R2Item::new);
        itemProviderMap2.put("v1_14_R1", V1_14_R1Item::new);
        itemProviderMap2.put("v1_15_R1", V1_15_R1Item::new);
        itemProviderMap2.put("v1_16_R1", V1_16_R1Item::new);
        itemProviderMap2.put("v1_16_R2", V1_16_R2Item::new);
        itemProviderMap2.put("v1_16_R3", V1_16_R3Item::new);
        itemProviderMap2.put("v1_17_R1", V1_17_R1Item::new);
        itemProviderMap2.put("v1_18_R1", V1_18_R1Item::new);
        itemProviderMap2.put("v1_18_R2", V1_18_R2Item::new);
        itemProviderMap2.put("v1_19_R1", V1_19_R1Item::new);
        itemProviderMap2.put("v1_19_R2", V1_19_R2Item::new);
        itemProviderMap2.put("v1_19_R3", V1_19_R3Item::new);
        itemProviderMap2.put("v1_20_R1", V1_20_R1Item::new);
        itemProviderMap2.put("v1_20_R2", V1_20_R2Item::new);
    }

    public static Item item(ItemStack itemStack) {
        return itemProviderMap1.getOrDefault(CrypticLib.nmsVersion(), i -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(itemStack);
    }

    public static Item item(String material, AbstractNbtTagCompound nbtCompound) {
        return itemProviderMap2.getOrDefault(CrypticLib.nmsVersion(), (material1, nbtCompound1) -> {
            throw new RuntimeException("Unsupported version: " + CrypticLib.nmsVersion());
        }).apply(material, nbtCompound);
    }

    public static Item item(ConfigurationSection config) {
        String material = config.getString("material");
        AbstractNbtTagCompound nbtCompound = null;
        if (config.isConfigurationSection("nbt")) {
            nbtCompound = NbtManager.config2NbtCompound(config.getConfigurationSection("nbt"));
        }
        return item(material, nbtCompound);
    }

    public static void regItemProvider(String nmsVersion, Function<ItemStack, Item> provider1, BiFunction<String, AbstractNbtTagCompound, Item> provider2) {
        itemProviderMap1.put(nmsVersion, provider1);
        itemProviderMap2.put(nmsVersion, provider2);
    }

}
