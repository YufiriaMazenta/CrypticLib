package crypticlib.config.entry;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;

public class DoubleConfig extends ConfigNode<Double> {

    public DoubleConfig(@NotNull String key, @NotNull Double def) {
        super(key, def);
    }

}
