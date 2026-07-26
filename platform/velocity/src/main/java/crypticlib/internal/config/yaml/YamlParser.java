package crypticlib.internal.config.yaml;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.core.io.ParsingMode;
import org.jetbrains.annotations.NotNull;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.comments.CommentLine;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.nodes.*;
import org.yaml.snakeyaml.reader.UnicodeReader;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;

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
        CommentedConfig config = this.configFormat.createConfig(LinkedHashMap::new);
        this.parse(reader, config, ParsingMode.MERGE);
        return config;
    }

    @Override
    public void parse(Reader reader, Config destination, ParsingMode parsingMode) {
        try {
            String text = readText(reader);
            configFormat.loaderOptions().setProcessComments(true);

            MappingNode mappingNode;
            try (Reader unicodeReader = new UnicodeReader(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)))) {
                Node rawNode = yaml.compose(unicodeReader);
                try {
                    mappingNode = (MappingNode) rawNode;
                } catch (ClassCastException e) {
                    throw new YAMLException("Failed to parse yaml content", e);
                }
            } catch (YAMLException | IOException | ClassCastException e) {
                throw new RuntimeException(e);
            }
            parsingMode.prepareParsing(destination);
            if (mappingNode != null) {
                // 不再把文件头部注释(首键上方最后一个空行之前的块注释)剥离到从不回写的根节点上,
                // 保留在首个键节点的块注释里, 避免 saveConfig 回写时头部注释永久丢失。
                // 说明: 仅键上方的块注释会被保留, 值后的行内注释暂不支持。
                fromNodeTree(mappingNode, destination);
            }
        } catch (Exception e) {
            throw new ParsingException("YAML parsing failed", e);
        }
    }

    private String readText(Reader reader) {
        StringBuilder contentBuilder = new StringBuilder();
        try {
            String line;
            while ((line = ((BufferedReader) reader).readLine()) != null) {
                contentBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return contentBuilder.toString();
    }

    private void fromNodeTree(@NotNull MappingNode input, @NotNull Config config) {
        constructor.flattenMapping(input);
        for (NodeTuple nodeTuple : input.getValue()) {
            Node keyNode = nodeTuple.getKeyNode();
            String key = String.valueOf(constructor.construct(keyNode));
            Node valueNode = nodeTuple.getValueNode();

            while (valueNode instanceof AnchorNode) {
                valueNode = ((AnchorNode) valueNode).getRealNode();
            }

            List<String> singleKeyPath = Collections.singletonList(key);
            if (valueNode instanceof MappingNode) {
                Config subConfig = config.createSubConfig();
                fromNodeTree((MappingNode) valueNode, subConfig);
                config.set(singleKeyPath, subConfig);
            } else if (valueNode instanceof SequenceNode) {
                List<Object> objects = new ArrayList<>();
                fromNodeList((SequenceNode) valueNode, objects);
                config.set(singleKeyPath, objects);
            } else {
                config.set(singleKeyPath, constructor.construct(valueNode));
            }

            if (config instanceof CommentedConfig) {
                List<CommentLine> blockComments = keyNode.getBlockComments();
                if (blockComments == null || blockComments.isEmpty()) {
                    continue;
                }

                String commentJsonArray = CommentLoader.commentLineList2JsonArray(blockComments);
                ((CommentedConfig) config).setComment(singleKeyPath, commentJsonArray);
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