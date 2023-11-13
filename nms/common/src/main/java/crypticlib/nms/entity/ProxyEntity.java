package crypticlib.nms.entity;

import crypticlib.nms.nbt.NbtTagCompound;
import org.bukkit.entity.Entity;

public abstract class ProxyEntity {

    private NbtTagCompound nbtTagCompound;
    private Entity bukkit;

    public ProxyEntity(Entity entity) {
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

    public ProxyEntity setEntity(Entity bukkit) {
        this.bukkit = bukkit;
        return this;
    }

    public abstract ProxyEntity saveToEntity();

    public abstract void fromNms();

}
