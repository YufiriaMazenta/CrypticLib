package crypticlib.lifecycle;

import crypticlib.lifecycle.Loader;
import org.bukkit.plugin.Plugin;

public interface BukkitLoader extends Loader<Plugin> {

    void load(Plugin plugin);

}
