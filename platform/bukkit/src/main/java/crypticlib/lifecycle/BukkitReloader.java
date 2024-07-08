package crypticlib.lifecycle;

import org.bukkit.plugin.Plugin;

public interface BukkitReloader extends Reloader<Plugin> {

    void reload(Plugin plugin);

}
