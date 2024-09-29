package crypticlib.internal.config.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.utils.TransformingList;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.yaml.snakeyaml.Yaml;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class YamlParser implements ConfigParser<Config> {

    private final Yaml yaml;
    private final ConfigFormat<Config> configFormat;

    public YamlParser(YamlFormat configFormat) {
        this.yaml = configFormat.yaml();
        this.configFormat = configFormat;
    }

    @Override
    public ConfigFormat<Config> getFormat() {
        return this.configFormat;
    }

    @Override
    public Config parse(Reader reader) {
        Config config = this.configFormat.createConfig();
        this.parse(reader, config, ParsingMode.MERGE);
        return config;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
        try {
            Map<String, Object> load = (Map<String, Object>) this.yaml.loadAs(reader, Map.class);
            Map<String, Object> wrappedMap = this.wrap(load);
            parsingMode.prepareParsing(destination);
            if (parsingMode == ParsingMode.ADD) {
                for (Map.Entry<String, Object> entry : wrappedMap.entrySet()) {
                    destination.valueMap().putIfAbsent(entry.getKey(), entry.getValue());
                }
            } else {
                destination.valueMap().putAll(wrappedMap);
            }

        } catch (Exception e) {
            throw new ParsingException("YAML parsing failed", e);
        }
    }

    private Map<String, Object> wrap(Map<String, Object> map) {
        if (map == null) {
            map = new HashMap<>();
        }
        return new TransformingMap<>(map, this::wrap, (v) -> v, (v) -> v);
    }

    private List<Object> wrapList(List<Object> list) {
        if (list == null) {
            list = new ArrayList<>();
        }
        return new TransformingList<>(list, this::wrap, (v) -> v, (v) -> v);
    }

    @SuppressWarnings("unchecked")
    private Object wrap(Object value) {
        if (value instanceof Map) {
            Map<String, Object> map = this.wrap((Map<String, Object>) value);
            return Config.wrap(map, this.configFormat);
        } else if (value instanceof List) {
            return this.wrapList((List<Object>) value);
        } else {
            return value == null ? NullObject.NULL_OBJECT : value;
        }
    }
}