package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ConfigSectionConfig extends VelocityConfigNode<CommentedConfig> {

    public ConfigSectionConfig(@NotNull String key) {
        super(key, CommentedConfig.inMemoryConcurrent());
    }

    public ConfigSectionConfig(@NotNull String key, String comment) {
        super(key, CommentedConfig.inMemoryConcurrent(), comment);
    }

    public ConfigSectionConfig(@NotNull String key, List<String> comments) {
        super(key, CommentedConfig.inMemoryConcurrent(), comments);
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull Map<String, Object> def) {
        this(key, def, new ArrayList<>());
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull Map<String, Object> def, @NotNull String comment) {
        this(key, def, new ArrayList<>(Collections.singletonList(comment)));
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull Map<String, Object> def, @NotNull List<String> defComments) {
        super(key, CommentedConfig.inMemoryConcurrent(), defComments);
        for (Map.Entry<String, Object> entry : def.entrySet()) {
            this.def.set(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void load(@NotNull CommentedConfig config) {
        setValue(config.getOrElse(key, def));
        setComments(configContainer.configWrapper().getComments(key));
    }

}
