package crypticlib.dependency;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * XML 解析基类，用于解析 Maven POM 文件
 */
public abstract class AbstractXmlParser {

    private static final Pattern SUBSTITUTION_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    @NotNull
    protected static String find(@NotNull String name, @NotNull Element node) throws ParseException {
        return find(name, node, null);
    }

    @NotNull
    protected static String find(@NotNull String name, @NotNull Element node, @Nullable String def) throws ParseException {
        NodeList list = node.getChildNodes();
        for (int i = 0; i < list.getLength(); ++i) {
            Node n = list.item(i);
            if (n.getNodeName().equals(name)) {
                try {
                    return replaceVariables(n.getTextContent(), node.getOwnerDocument().getDocumentElement());
                } catch (ParseException ex) {
                    if (def == null) {
                        throw ex;
                    } else {
                        return def;
                    }
                }
            }
        }
        list = node.getElementsByTagName(name);
        if (list.getLength() > 0) {
            try {
                return replaceVariables(list.item(0).getTextContent(), node.getOwnerDocument().getDocumentElement());
            } catch (ParseException ex) {
                if (def == null) {
                    throw ex;
                } else {
                    return def;
                }
            }
        }
        if (def == null) {
            throw new ParseException(String.format("Unable to find required tag '%s' in node", name), -1);
        }
        return def;
    }

    @NotNull
    private static String replaceVariables(@NotNull String text, @NotNull Element pom) throws ParseException {
        Matcher matcher = SUBSTITUTION_PATTERN.matcher(text);
        while (matcher.find()) {
            text = matcher.replaceFirst(getReplacement(matcher.group(1), pom));
        }
        return text;
    }

    @NotNull
    private static String getReplacement(@NotNull String key, @NotNull Element pom) throws ParseException {
        if (key.startsWith("project.")) {
            return find(key.substring("project.".length()), pom);
        } else if (key.startsWith("pom.")) {
            return find(key.substring("pom.".length()), pom);
        } else {
            throw new ParseException(String.format("Unknown variable '%s'", key), -1);
        }
    }
}
