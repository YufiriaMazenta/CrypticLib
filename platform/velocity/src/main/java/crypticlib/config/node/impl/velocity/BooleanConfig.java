package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.Config;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

public class BooleanConfig extends VelocityConfigNode<Boolean> {

    public BooleanConfig(@NotNull String key, @NotNull Boolean def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Config config) {
        setValue(config.getOrElse(key, def));
    }

}
