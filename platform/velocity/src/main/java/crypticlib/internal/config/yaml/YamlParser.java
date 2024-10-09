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
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.reader.UnicodeReader;
import org.yaml.snakeyaml.representer.Representer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * @author Electronwill, YufiriaMazenta
 */
public final class YamlParser implements ConfigParser<CommentedConfig> {

    private final Yaml yaml;
    private final YamlFormat configFormat;
    private final YamlConstructor constructor;

    public YamlParser(YamlFormat yamlFormat) {
        this.yaml = yamlFormat.yaml();
        this.configFormat = yamlFormat;
        this.constructor = new YamlConstructor(yamlFormat.loaderOptions());
    }

    @Override
    public ConfigFormat<CommentedConfig> getFormat() {
        return this.configFormat;
    }

    @Override
    public CommentedConfig parse(Reader reader) {
        CommentedConfig config = this.configFormat.createConfig();
        this.parse(reader, config, ParsingMode.MERGE);
        return config;
    }

    @Override
    public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
        try {
            MappingNode mappingNode;
            Node rawNode = yaml.compose(reader);
            try {
                mappingNode = (MappingNode) rawNode;
            } catch (ClassCastException e) {
                throw new YAMLException("Top level is not a Map.");
            }
            parsingMode.prepareParsing(destination);
            fromNodeTree(mappingNode, destination);
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

    private void fromNodeTree(MappingNode input, @NotNull Config config) {
        if (input == null)
            return;
        constructor.flattenMapping(input);
        for (NodeTuple nodeTuple : input.getValue()) {
            Node keyNode = nodeTuple.getKeyNode();
            String key = String.valueOf(constructor.construct(keyNode));
            Node valueNode = nodeTuple.getValueNode();

            while (valueNode instanceof AnchorNode) {
                valueNode = ((AnchorNode) valueNode).getRealNode();
            }

            if (valueNode instanceof MappingNode) {
                Config subConfig = config.createSubConfig();
                fromNodeTree((MappingNode) valueNode, subConfig);
                config.set(key, subConfig);
            } else if (valueNode instanceof SequenceNode) {
                List<Object> objects = new ArrayList<>();
                fromNodeList((SequenceNode) valueNode, objects);
                config.set(key, objects);
            } else {
                config.set(key, constructor.construct(valueNode));
            }

            if (config instanceof CommentedConfig) {
                List<CommentLine> blockComments = keyNode.getBlockComments();
                if (blockComments == null || blockComments.isEmpty()) {
                    continue;
                }
                String comment = blockComments.get(0).getValue();
                ((CommentedConfig) config).setComment(key, comment);
            }
        }
    }

    private void fromNodeList(@NotNull SequenceNode input, @NotNull List<Object> objects) {
        for (Node node : input.getValue()) {
            if (node instanceof MappingNode) {
                CommentedConfig subConfig = CommentedConfig.inMemory();
                fromNodeTree((MappingNode) node, subConfig);
                objects.add(subConfig);
            } else if (node instanceof SequenceNode) {
                List<Object> objects2 = new ArrayList<>();
                fromNodeList((SequenceNode) node, objects2);
                objects.add(objects2);
            } else {
                objects.add(constructor.construct(node));
            }
        }
    }

}