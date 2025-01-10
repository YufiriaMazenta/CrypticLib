package crypticlib.util;

import crypticlib.MinecraftVersion;
import org.bukkit.Material;
import org.jetbrains.annotations.Contract;

public class MaterialHelper {

    public static Material matchMaterial(String materialKey) {
        String[] keySplit = materialKey.split(":");
        if (keySplit.length < 2) {
            return Material.matchMaterial(materialKey);
        }
        if (keySplit[0].equalsIgnoreCase("minecraft")) {
            return Material.matchMaterial(materialKey);
        }
        Material material = Material.matchMaterial(materialKey);
        if (material == null) {
            material = Material.matchMaterial(materialKey.replace(":", "_"));
        }
        return material;
    }

    @Contract("null -> null; !null -> !null")
    @SuppressWarnings("removal")
    public static String getTranslationKey(Material material) {
        if (material == null) {
            return null;
        }
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_19_3)) {
            return material.getTranslationKey();
        } else {
            String materialKey = material.getKey().getKey();
            if (material.isBlock()){
                return "block.minecraft." + materialKey;
            } else {
                return "item.minecraft." + materialKey;
            }
        }
    }

}
