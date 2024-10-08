package crypticlib.config.node;

import com.electronwill.nightconfig.core.Config;
import org.jetbrains.annotations.NotNull;

public abstract class VelocityConfigNode<T> extends ConfigNode<T, Config> {

    public VelocityConfigNode(@NotNull String key, @NotNull T def) {
        super(key, def);
    }

    @Override
    public void saveDef(@NotNull Config config) {
        //加载默认值
        if (!config.contains(key)) {
            setValue(def);
        }
    }

}
