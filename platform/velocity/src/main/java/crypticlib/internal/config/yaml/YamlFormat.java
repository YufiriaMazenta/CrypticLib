package crypticlib.internal.config.yaml;

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

public class YamlFormat implements ConfigFormat<Config> {

    private static final ThreadLocal<YamlFormat> LOCAL_DEFAULT_FORMAT = ThreadLocal.withInitial(
        () -> {
            DumperOptions yamlDumperOptions = new DumperOptions();
            yamlDumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            LoaderOptions yamlLoaderOptions = new LoaderOptions();
            yamlLoaderOptions.setMaxAliasesForCollections(Integer.MAX_VALUE);
            yamlLoaderOptions.setCodePointLimit(Integer.MAX_VALUE);
            Yaml yaml = new Yaml(new Constructor(yamlLoaderOptions), new Representer(yamlDumperOptions), yamlDumperOptions);
            return new YamlFormat(yaml);
        });

    public static YamlFormat defaultInstance() {
        return LOCAL_DEFAULT_FORMAT.get();
    }

    public static YamlFormat configuredInstance(Yaml yaml) {
        return new YamlFormat(yaml);
    }
    private final Yaml yaml;

    public static Config newConfig() {
        return defaultInstance().createConfig();
    }

    public static Config newConfig(Supplier<Map<String, Object>> mapCreator) {
        return defaultInstance().createConfig(mapCreator);
    }

    public static Config newConcurrentConfig() {
        return defaultInstance().createConcurrentConfig();
    }

    YamlFormat(Yaml yaml) {
        this.yaml = yaml;
    }

    @Override
    public ConfigWriter createWriter() {
        return new YamlWriter(this.yaml);
    }

    @Override
    public ConfigParser<Config> createParser() {
        return new YamlParser(this);
    }

    @Override
    public Config createConfig(Supplier<Map<String, Object>> mapCreator) {
        return Config.of(mapCreator, this);
    }

    @Override
    public boolean supportsComments() {
        return false;
    }

    public Yaml yaml() {
        return yaml;
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
