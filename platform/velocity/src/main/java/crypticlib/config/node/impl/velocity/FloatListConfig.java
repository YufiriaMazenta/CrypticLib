package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.Config;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FloatListConfig extends VelocityConfigNode<List<Float>> {

    public FloatListConfig(@NotNull String key, @NotNull List<Float> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Config config) {
        saveDef(config);
        setValue(config.getOrElse(key, def));
    }

}
