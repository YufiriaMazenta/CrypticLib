package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PomParser {

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    @NotNull
    public static List<PomDependency> parseDependencies(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version) throws IOException {
        String repositoryUrl = repository;
        if (!repositoryUrl.endsWith("/")) {
            repositoryUrl += "/";
        }

        String groupIdPath = groupId.replace('.', '/');
        String pomUrl = repositoryUrl + groupIdPath + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";

        try (InputStream is = new URL(pomUrl).openStream()) {
            String pomContent = new String(readAllBytes(is), "UTF-8");
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

        int depsStart = pomContent.indexOf("<dependencies>");
        if (depsStart == -1) {
            return Collections.emptyList();
        }

        int depsEnd = pomContent.indexOf("</dependencies>", depsStart);
        if (depsEnd == -1) {
            return Collections.emptyList();
        }

        String depsSection = pomContent.substring(depsStart, depsEnd);
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

        String repositoryUrl = repository;
        if (!repositoryUrl.endsWith("/")) {
            repositoryUrl += "/";
        }

        String parentGroupIdPath = parentGroupId.replace('.', '/');
        String parentPomUrl = repositoryUrl + parentGroupIdPath + "/" + parentArtifactId + "/" + parentVersion + "/" + parentArtifactId + "-" + parentVersion + ".pom";

        try (InputStream is = new URL(parentPomUrl).openStream()) {
            return new String(readAllBytes(is), "UTF-8");
        } catch (IOException e) {
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

    private static byte[] readAllBytes(@NotNull InputStream is) throws IOException {
        byte[] buffer = new byte[8192];
        int bytesRead;
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        while ((bytesRead = is.read(buffer)) != -1) {
            bos.write(buffer, 0, bytesRead);
        }
        return bos.toByteArray();
    }

}