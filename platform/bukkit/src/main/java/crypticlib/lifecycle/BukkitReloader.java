package crypticlib.lifecycle;

import crypticlib.lifecycle.Reloader;
import org.bukkit.plugin.Plugin;

public interface BukkitReloader extends Reloader<Plugin> {

    void reload(Plugin plugin);

}
