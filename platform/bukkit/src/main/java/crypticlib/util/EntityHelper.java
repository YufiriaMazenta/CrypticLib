package crypticlib.util;

import crypticlib.MinecraftVersion;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.Contract;

public class EntityHelper {

    @Contract("null -> null; !null -> !null")
    @SuppressWarnings("removal")
    public static String getTranslationKey(EntityType type) {
        if (MinecraftVersion.current().afterOrEquals(MinecraftVersion.V1_19_3)) {
            return type.getTranslationKey();
        } else {
            return "entity.minecraft." + type.getKey().getKey();
        }
    }

}
