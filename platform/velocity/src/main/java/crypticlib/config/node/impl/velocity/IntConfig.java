package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.Config;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

public class IntConfig extends VelocityConfigNode<Integer> {

    public IntConfig(@NotNull String key, @NotNull Integer def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Config config) {
        saveDef(config);
        setValue(config.getIntOrElse(key, def));
    }

}
