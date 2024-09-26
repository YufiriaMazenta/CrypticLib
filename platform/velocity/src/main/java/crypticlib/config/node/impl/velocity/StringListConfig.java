package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.Config;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringListConfig extends VelocityConfigNode<List<String>> {

    public StringListConfig(@NotNull String key, @NotNull List<String> def) {
        super(key, def);
    }

    @Override
    public void load(@NotNull Config config) {
        saveDef(config);
        setValue(config.getOrElse(key, def));
    }

}
