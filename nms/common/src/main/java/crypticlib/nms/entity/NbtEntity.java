package crypticlib.nms.entity;

import crypticlib.nms.nbt.NbtTagCompound;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Entity;

/**
 * 代理Bukkit实体对象,用于直接操作NBT数据
 */
public abstract class NbtEntity {

    protected NbtTagCompound nbtTagCompound;
    protected Entity bukkit;

    protected NbtEntity(Entity entity) {
        this.bukkit = entity;
        fromBukkit();
    }

    public NbtTagCompound nbtTagCompound() {
        return nbtTagCompound;
    }

    public NbtEntity setNbtTagCompound(NbtTagCompound nbtTagCompound) {
        this.nbtTagCompound = nbtTagCompound;
        return this;
    }

    public Entity bukkit() {
        return bukkit;
    }

    public NbtEntity setBukkitEntity(Entity bukkit) {
        this.bukkit = bukkit;
        return this;
    }

    public abstract NbtEntity saveNbtToEntity();

    public abstract void fromBukkit();

    public HoverEvent toHover() {
        return new HoverEvent(
            HoverEvent.Action.SHOW_ENTITY,
            new net.md_5.bungee.api.chat.hover.content.Entity(
                bukkit.getType().getKey().toString(),
                bukkit.getUniqueId().toString(),
                new TextComponent(TextComponent.fromLegacyText(bukkit.getName()))
            )
        );
    }

}
