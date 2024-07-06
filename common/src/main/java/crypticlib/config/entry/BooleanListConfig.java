package crypticlib.config.entry;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BooleanListConfig extends ConfigNode<List<Boolean>> {

    public BooleanListConfig(@NotNull String key, @NotNull List<Boolean> def) {
        super(key, def);
    }

}
