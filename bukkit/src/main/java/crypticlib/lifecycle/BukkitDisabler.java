package crypticlib.lifecycle;

import crypticlib.lifecycle.Disabler;
import org.bukkit.plugin.Plugin;

public interface BukkitDisabler extends Disabler<Plugin> {

    void disable(Plugin plugin);

}
