package crypticlib.config.node.impl.velocity;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import crypticlib.config.node.VelocityConfigNode;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConfigSectionConfig extends VelocityConfigNode<CommentedConfig> {

    public ConfigSectionConfig(@NotNull String key) {
        this(key, CommentedConfig.inMemory());
    }

    public ConfigSectionConfig(@NotNull String key, String comment) {
        super(key, CommentedConfig.inMemory(), comment);
    }

    public ConfigSectionConfig(@NotNull String key, List<String> comments) {
        super(key, CommentedConfig.inMemory(), comments);
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull CommentedConfig def) {
        super(key, def);
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull CommentedConfig def, @NotNull String comment) {
        super(key, def, comment);
    }

    public ConfigSectionConfig(@NotNull String key, @NotNull CommentedConfig def, @NotNull List<String> defComments) {
        super(key, def, defComments);
    }

    @Override
    public void load(@NotNull CommentedConfig config) {
        setValue(config.getOrElse(key, def));
        setComments(configContainer.configWrapper().getComments(key));
    }

}
