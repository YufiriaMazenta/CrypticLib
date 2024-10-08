package crypticlib.internal.config.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import com.electronwill.nightconfig.core.utils.TransformingList;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.AnchorNode;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Electronwill, YufiriaMazenta
 */
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

            MappingNode mappingNode;
            Node rawNode = yaml.compose(reader);
            try {
                mappingNode = (MappingNode) rawNode;
            } catch (ClassCastException e) {
                throw new YAMLException("Top level is not a Map.");
            }
            Map<String, Object> valueMap = new LinkedHashMap<>();
            Map<String, String> commentMap = new LinkedHashMap<>();
            //TODO 解析整个mappingnode
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