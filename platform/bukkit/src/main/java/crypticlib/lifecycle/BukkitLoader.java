package crypticlib.lifecycle;

import org.bukkit.plugin.Plugin;

public interface BukkitLoader extends Loader<Plugin> {

    void load(Plugin plugin);

}
