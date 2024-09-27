package crypticlib.internal.config.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.yaml.snakeyaml.Yaml;

public enum YamlFormat implements ConfigFormat<Config> {

    INSTANCE(new Yaml());

    private final Yaml yaml;

    public static Config newConfig() {
        return INSTANCE.createConfig();
    }

    public static Config newConfig(Supplier<Map<String, Object>> mapCreator) {
        return INSTANCE.createConfig(mapCreator);
    }

    public static Config newConcurrentConfig() {
        return INSTANCE.createConcurrentConfig();
    }

    private YamlFormat(Yaml yaml) {
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

    @Override
    public boolean supportsType(Class<?> type) {
        return type == null || type.isEnum() || type == Boolean.class || type == String.class || type == Date.class || type == java.sql.Date.class || type == Timestamp.class || type == byte[].class || type == Object[].class || Number.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type) || List.class.isAssignableFrom(type) || Config.class.isAssignableFrom(type);
    }

    public Yaml yaml() {
        return yaml;
    }

}
