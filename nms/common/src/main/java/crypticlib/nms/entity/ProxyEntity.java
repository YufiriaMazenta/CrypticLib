package crypticlib.nms.entity;

import crypticlib.nms.nbt.NbtTagCompound;
import org.bukkit.entity.Entity;

/**
 * 代理Bukkit实体对象,用于直接操作NBT数据
 */
public abstract class ProxyEntity {

    private NbtTagCompound nbtTagCompound;
    private Entity bukkit;

    protected ProxyEntity(Entity entity) {
        this.bukkit = entity;
        fromNms();
    }

    public NbtTagCompound nbtTagCompound() {
        return nbtTagCompound;
    }

    public ProxyEntity setNbtTagCompound(NbtTagCompound nbtTagCompound) {
        this.nbtTagCompound = nbtTagCompound;
        return this;
    }

    public Entity bukkitEntity() {
        return bukkit;
    }

    public ProxyEntity setBukkitEntity(Entity bukkit) {
        this.bukkit = bukkit;
        return this;
    }

    public abstract ProxyEntity saveNbtToEntity();

    public abstract void fromNms();

}
