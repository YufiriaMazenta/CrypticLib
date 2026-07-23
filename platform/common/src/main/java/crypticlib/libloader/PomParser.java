package crypticlib.libloader;

import crypticlib.util.IOHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PomParser {

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    @NotNull
    private static String normalizeRepository(@NotNull String repository) {
        return repository.endsWith("/") ? repository : repository + "/";
    }

    @NotNull
    private static String buildPomUrl(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        return normalizeRepository(repository) + groupId.replace('.', '/') + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";
    }

    @NotNull
    public static List<PomDependency> parseDependencies(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version) throws IOException, URISyntaxException {
        String pomUrl = buildPomUrl(repository, groupId, artifactId, version);

        try (InputStream is = new URI(pomUrl).toURL().openStream()) {
            String pomContent = new String(IOHelper.readBytes(is), StandardCharsets.UTF_8);
            Map<String, String> properties = parseProperties(pomContent);
            properties.put("project.version", version);
            properties.put("project.groupId", groupId);
            properties.put("project.artifactId", artifactId);

            String parentPomContent = resolveParentPom(repository, pomContent, properties);
            if (parentPomContent != null) {
                Map<String, String> parentProps = parseProperties(parentPomContent);
                for (Map.Entry<String, String> entry : parentProps.entrySet()) {
                    properties.putIfAbsent(entry.getKey(), entry.getValue());
                }
            }

            return parseDependenciesFromPom(pomContent, repository, properties);
        }
    }

    @NotNull
    private static List<PomDependency> parseDependenciesFromPom(@NotNull String pomContent, @NotNull String repository, @NotNull Map<String, String> properties) {
        List<PomDependency> dependencies = new ArrayList<>();

        String depsSection = findDependenciesSection(pomContent);
        if (depsSection == null) {
            return Collections.emptyList();
        }

        int pos = 0;
        while (true) {
            int depStart = depsSection.indexOf("<dependency>", pos);
            if (depStart == -1) break;

            int depEnd = depsSection.indexOf("</dependency>", depStart);
            if (depEnd == -1) break;

            String depXml = depsSection.substring(depStart, depEnd + "</dependency>".length());
            PomDependency dep = parseDependencyElement(depXml, properties);
            if (dep != null) {
                dependencies.add(dep);
            }

            pos = depEnd + "</dependency>".length();
        }

        return dependencies;
    }

    @Nullable
    private static String findDependenciesSection(@NotNull String pomContent) {
        int dmStart = pomContent.indexOf("<dependencyManagement>");
        int dmEnd = dmStart == -1 ? -1 : findElementEnd(pomContent, dmStart, "dependencyManagement");

        int pos = 0;
        while (true) {
            int depsStart = pomContent.indexOf("<dependencies>", pos);
            if (depsStart == -1) {
                return null;
            }

            if (dmStart != -1 && dmEnd != -1 && depsStart > dmStart && depsStart < dmEnd) {
                pos = dmEnd;
                continue;
            }

            int depsEnd = findElementEnd(pomContent, depsStart, "dependencies");
            if (depsEnd == -1) {
                return null;
            }
            return pomContent.substring(depsStart + "<dependencies>".length(), depsEnd);
        }
    }

    private static int findElementEnd(@NotNull String content, int start, @NotNull String tag) {
        String openTag = "<" + tag + ">";
        String closeTag = "</" + tag + ">";
        int depth = 0;
        int pos = start;
        while (pos < content.length()) {
            int nextOpen = content.indexOf(openTag, pos);
            int nextClose = content.indexOf(closeTag, pos);

            if (nextClose == -1) {
                return -1;
            }

            if (nextOpen != -1 && nextOpen < nextClose) {
                depth++;
                pos = nextOpen + openTag.length();
            } else {
                depth--;
                if (depth == 0) {
                    return nextClose;
                }
                pos = nextClose + closeTag.length();
            }
        }
        return -1;
    }

    @NotNull
    public static String parsePackaging(@NotNull String pomContent) {
        String packaging = extractTag(pomContent, "packaging");
        return packaging != null ? packaging : "jar";
    }

    @Nullable
    public static String fetchPomContent(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        String pomUrl = buildPomUrl(repository, groupId, artifactId, version);
        try (InputStream is = new URI(pomUrl).toURL().openStream()) {
            return new String(IOHelper.readBytes(is), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            return null;
        }
    }

    @NotNull
    private static Map<String, String> parseProperties(@NotNull String pomContent) {
        Map<String, String> properties = new HashMap<>();

        int propsStart = pomContent.indexOf("<properties>");
        if (propsStart == -1) {
            return properties;
        }

        int propsEnd = pomContent.indexOf("</properties>", propsStart);
        if (propsEnd == -1) {
            return properties;
        }

        String propsSection = pomContent.substring(propsStart, propsEnd);
        int pos = 0;

        while (true) {
            int tagStart = propsSection.indexOf("<", pos);
            if (tagStart == -1) break;

            int tagEnd = propsSection.indexOf(">", tagStart);
            if (tagEnd == -1) break;

            String tagName = propsSection.substring(tagStart + 1, tagEnd).trim();
            if (tagName.startsWith("/") || tagName.contains(" ")) {
                pos = tagEnd + 1;
                continue;
            }

            String closeTag = "</" + tagName + ">";
            int closeIdx = propsSection.indexOf(closeTag, tagEnd);
            if (closeIdx == -1) {
                pos = tagEnd + 1;
                continue;
            }

            String value = propsSection.substring(tagEnd + 1, closeIdx).trim();
            properties.put(tagName, value);
            pos = closeIdx + closeTag.length();
        }

        return properties;
    }

    @Nullable
    private static String resolveParentPom(@NotNull String repository, @NotNull String pomContent, @NotNull Map<String, String> properties) {
        int parentStart = pomContent.indexOf("<parent>");
        if (parentStart == -1) {
            return null;
        }

        int parentEnd = pomContent.indexOf("</parent>", parentStart);
        if (parentEnd == -1) {
            return null;
        }

        String parentSection = pomContent.substring(parentStart, parentEnd);
        String parentGroupId = extractTag(parentSection, "groupId");
        String parentArtifactId = extractTag(parentSection, "artifactId");
        String parentVersion = extractTag(parentSection, "version");

        if (parentGroupId == null || parentArtifactId == null || parentVersion == null) {
            return null;
        }

        parentVersion = resolveProperty(parentVersion, properties);

        String parentPomUrl = buildPomUrl(repository, parentGroupId, parentArtifactId, parentVersion);

        try (InputStream is = new URI(parentPomUrl).toURL().openStream()) {
            return new String(IOHelper.readBytes(is), StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            return null;
        }
    }

    @Nullable
    private static PomDependency parseDependencyElement(@NotNull String xml, @NotNull Map<String, String> properties) {
        String groupId = extractTag(xml, "groupId");
        String artifactId = extractTag(xml, "artifactId");
        String version = extractTag(xml, "version");
        String scope = extractTag(xml, "scope");
        String optional = extractTag(xml, "optional");

        if (groupId == null || artifactId == null) {
            return null;
        }

        groupId = resolveProperty(groupId, properties);
        artifactId = resolveProperty(artifactId, properties);
        if (version != null) {
            version = resolveProperty(version, properties);
        }
        if (scope != null) {
            scope = resolveProperty(scope, properties);
        }

        boolean isOptional = "true".equalsIgnoreCase(optional);
        return new PomDependency(groupId, artifactId, version, scope, isOptional);
    }

    @NotNull
    private static String resolveProperty(@NotNull String value, @NotNull Map<String, String> properties) {
        Matcher matcher = PROPERTY_PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String propertyName = matcher.group(1);
            String replacement = properties.getOrDefault(propertyName, matcher.group(0));
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Nullable
    private static String extractTag(@NotNull String xml, @NotNull String tag) {
        String startTag = "<" + tag + ">";
        String endTag = "</" + tag + ">";

        int start = xml.indexOf(startTag);
        if (start == -1) return null;
        start += startTag.length();

        int end = xml.indexOf(endTag, start);
        if (end == -1) return null;

        return xml.substring(start, end).trim();
    }

}