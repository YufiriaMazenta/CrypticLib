package crypticlib;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class BukkitEntity extends BukkitInvoker {

    protected final @NotNull UUID uuid;

    public BukkitEntity(@NotNull Entity entity) {
        super(entity);
        this.uuid = entity.getUniqueId();
    }

    public @NotNull UUID getUniqueId() {
        return uuid;
    }

    public static BukkitEntity byEntity(@NotNull Entity entity) {
        return new BukkitEntity(entity);
    }

}
