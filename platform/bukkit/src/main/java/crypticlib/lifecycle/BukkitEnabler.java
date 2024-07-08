package crypticlib.lifecycle;

import crypticlib.lifecycle.Enabler;
import org.bukkit.plugin.Plugin;

public interface BukkitEnabler extends Enabler<Plugin> {

    void enable(Plugin plugin);

}
