package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PomParser {

    @NotNull
    public static List<PomDependency> parseDependencies(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version) throws IOException {
        String repositoryUrl = repository;
        if (!repositoryUrl.endsWith("/")) {
            repositoryUrl += "/";
        }

        String groupIdPath = groupId.replace('.', '/');
        String pomUrl = repositoryUrl + groupIdPath + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";

        try (InputStream is = new URL(pomUrl).openStream()) {
            return parsePomStream(is, repository);
        }
    }

    @NotNull
    private static List<PomDependency> parsePomStream(@NotNull InputStream is, @NotNull String repository) throws IOException {
        List<PomDependency> dependencies = new ArrayList<>();
        String pomContent = new String(readAllBytes(is), "UTF-8");

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
            PomDependency dep = parseDependencyElement(depXml);
            if (dep != null) {
                dependencies.add(dep);
            }

            pos = depEnd + "</dependency>".length();
        }

        return dependencies;
    }

    @Nullable
    private static PomDependency parseDependencyElement(@NotNull String xml) {
        String groupId = extractTag(xml, "groupId");
        String artifactId = extractTag(xml, "artifactId");
        String version = extractTag(xml, "version");
        String scope = extractTag(xml, "scope");
        String optional = extractTag(xml, "optional");

        if (groupId == null || artifactId == null) {
            return null;
        }

        boolean isOptional = "true".equalsIgnoreCase(optional);
        return new PomDependency(groupId, artifactId, version, scope, isOptional);
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
