package crypticlib.internal.config.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.NullObject;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.utils.TransformingList;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.comments.CommentType;
import org.yaml.snakeyaml.nodes.MappingNode;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Electronwill, YufiriaMazenta
 */
public final class YamlWriter implements ConfigWriter {

    private final Yaml yaml;

    public YamlWriter(Yaml yaml) {
        this.yaml = yaml;
    }

    @Override
    public void write(UnmodifiableConfig config, Writer writer) {
        try {
            MappingNode nodeTree = toNodeTree(config);
            this.yaml.serialize(nodeTree, writer);
        } catch (Exception e) {
            throw new WritingException("YAML writing failed", e);
        }
    }

    private static Map<String, Object> unwrap(UnmodifiableConfig config) {
        return new TransformingMap<>(config.valueMap(), YamlWriter::unwrap, (v) -> v, (v) -> v);
    }

    private static List<Object> unwrapList(List<Object> list) {
        return new TransformingList<>(list, YamlWriter::unwrap, (v) -> v, (v) -> v);
    }

    @SuppressWarnings("unchecked")
    private static Object unwrap(Object value) {
        if (value instanceof UnmodifiableConfig) {
            return unwrap((UnmodifiableConfig)value);
        } else if (value instanceof List) {
            return unwrapList((List<Object>) value);
        } else {
            return value == NullObject.NULL_OBJECT ? null : value;
        }
    }

    public MappingNode toNodeTree(UnmodifiableConfig config) {
        Map<String, Object> unwrappedMap = unwrap(config);
        List<NodeTuple> nodeTuples = new ArrayList<>();
        if (config instanceof CommentedConfig) {
            CommentedConfig commentedConfig = (CommentedConfig) config;
            Map<String, String> commentMap = commentedConfig.commentMap();
            unwrappedMap.forEach((key, value) -> {
                Node keyNode = yaml.represent(key);
                Node valueNode;
                if (value instanceof UnmodifiableConfig) {
                    valueNode = toNodeTree((UnmodifiableConfig)value);
                } else {
                    valueNode = yaml.represent(value);
                }
                String comment = commentMap.get(key);
                if (comment != null && !comment.trim().isEmpty()) {
                    keyNode.setBlockComments(Collections.singletonList(new CommentLine(null, null, comment, CommentType.BLOCK)));
                }
                nodeTuples.add(new NodeTuple(keyNode, valueNode));
            });
        } else {
            unwrappedMap.forEach((key, value) -> {
                Node keyNode = yaml.represent(key);
                Node valueNode;
                if (value instanceof UnmodifiableConfig) {
                    valueNode = toNodeTree((UnmodifiableConfig)value);
                } else {
                    valueNode = yaml.represent(value);
                }
                nodeTuples.add(new NodeTuple(keyNode, valueNode));
            });
        }
        return new MappingNode(Tag.MAP, nodeTuples, DumperOptions.FlowStyle.BLOCK);
    }

}
