package crypticlib.internal.config.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.representer.Representer;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * @author Electronwill, YufiriaMazenta
 */
public class YamlFormat implements ConfigFormat<CommentedConfig> {

    private static final ThreadLocal<YamlFormat> LOCAL_DEFAULT_FORMAT = ThreadLocal.withInitial(
        () -> {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            dumperOptions.setProcessComments(true);
            LoaderOptions loaderOptions = new LoaderOptions();
            loaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
            loaderOptions.setCodePointLimit(Integer.MAX_VALUE);
            loaderOptions.setProcessComments(true);
            return new YamlFormat(loaderOptions, dumperOptions);
        });

    public static YamlFormat defaultInstance() {
        return LOCAL_DEFAULT_FORMAT.get();
    }

    private final Yaml yaml;
    private final Constructor constructor;
    private final Representer representer;
    private final LoaderOptions loaderOptions;
    private final DumperOptions dumperOptions;

    public static CommentedConfig newConfig() {
        return defaultInstance().createConfig();
    }

    public static CommentedConfig newConfig(Supplier<Map<String, Object>> mapCreator) {
        return defaultInstance().createConfig(mapCreator);
    }

    public static CommentedConfig newConcurrentConfig() {
        return defaultInstance().createConcurrentConfig();
    }

    YamlFormat(LoaderOptions loaderOptions, DumperOptions dumperOptions) {
        this.loaderOptions = loaderOptions;
        this.dumperOptions = dumperOptions;
        this.constructor = new Constructor(loaderOptions);
        this.representer = new Representer(dumperOptions);
        this.yaml = new Yaml(constructor, representer, dumperOptions, loaderOptions);
    }

    @Override
    public ConfigWriter createWriter() {
        return new YamlWriter(this);
    }

    @Override
    public ConfigParser<CommentedConfig> createParser() {
        return new YamlParser(this);
    }

    @Override
    public CommentedConfig createConfig(Supplier<Map<String, Object>> mapCreator) {
        return CommentedConfig.of(mapCreator, this);
    }

    @Override
    public boolean supportsComments() {
        return true;
    }

    public Yaml yaml() {
        return yaml;
    }

    public Constructor constructor() {
        return constructor;
    }

    public Representer representer() {
        return representer;
    }

    public LoaderOptions loaderOptions() {
        return loaderOptions;
    }

    public DumperOptions dumperOptions() {
        return dumperOptions;
    }

    @Override
    public boolean supportsType(Class<?> type) {
        return type == null
            || type.isEnum()
            || type == Boolean.class
            || type == String.class
            || type == java.util.Date.class
            || type == java.sql.Date.class
            || type == java.sql.Timestamp.class
            || type == byte[].class
            || type == Object[].class
            || Number.class.isAssignableFrom(type)
            || Set.class.isAssignableFrom(type)
            || List.class.isAssignableFrom(type)
            || Config.class.isAssignableFrom(type);
    }

}
