package crypticlib.lifecycle;

import org.bukkit.plugin.Plugin;

public interface BukkitEnabler extends Enabler<Plugin> {

    void enable(Plugin plugin);

}
