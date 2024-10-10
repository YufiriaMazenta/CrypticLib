package crypticlib.internal.config.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import com.electronwill.nightconfig.core.io.WritingException;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.google.gson.Gson;
import crypticlib.chat.VelocityMsgSender;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.representer.Representer;

import java.io.Writer;
import java.nio.file.Path;
import java.util.*;

/**
 * @author Electronwill, YufiriaMazenta
 */
public final class YamlWriter implements ConfigWriter {

    private final Yaml yaml;
    private final Representer representer;
    private final Gson gson = new Gson();

    public YamlWriter(YamlFormat yamlFormat) {
        this.yaml = yamlFormat.yaml();
        this.representer = yamlFormat.representer();
    }

    @Override
    public void write(UnmodifiableConfig config, Path file, WritingMode writingMode) {
        ConfigWriter.super.write(config, file, writingMode);
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

    @SuppressWarnings("unchecked")
    public MappingNode toNodeTree(UnmodifiableConfig config) {
        List<NodeTuple> nodeTuples = new ArrayList<>();
        config.valueMap().forEach(
            (key, value) -> {
                Node keyNode = representer.represent(key);
                Node valueNode;
                if (value instanceof UnmodifiableConfig) {
                    valueNode = toNodeTree((UnmodifiableConfig) value);
                } else if (value instanceof List) {
                    valueNode = toSequenceNode((List<Object>) value);
                } else {
                    valueNode = representer.represent(value);
                }
                if (config instanceof CommentedConfig) {
                    CommentedConfig commentedConfig = (CommentedConfig) config;
                    String commentJsonArray = commentedConfig.getComment(key);
                    VelocityMsgSender.INSTANCE.debug(key + "'s comment: " + commentJsonArray);
                    if (commentJsonArray != null && !commentJsonArray.trim().isEmpty()) {
                        keyNode.setBlockComments(CommentLoader.loadCommentLineList(commentJsonArray));
                    }
                }
                nodeTuples.add(new NodeTuple(keyNode, valueNode));
            }
        );
        return new MappingNode(Tag.MAP, nodeTuples, DumperOptions.FlowStyle.BLOCK);
    }

    @SuppressWarnings("unchecked")
    public SequenceNode toSequenceNode(List<Object> objects) {
        List<Node> nodes = new ArrayList<>();
        objects.forEach(
            it -> {
                if (it instanceof UnmodifiableConfig) {
                    nodes.add(toNodeTree((UnmodifiableConfig) it));
                } else if (it instanceof List) {
                    nodes.add(toSequenceNode((List<Object>) it));
                } else {
                    nodes.add(representer.represent(it));
                }
            }
        );
        return new SequenceNode(Tag.SEQ, nodes, DumperOptions.FlowStyle.BLOCK);
    }

}
