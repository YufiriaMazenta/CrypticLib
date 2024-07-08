package crypticlib.util;

import org.bukkit.Material;

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

}
