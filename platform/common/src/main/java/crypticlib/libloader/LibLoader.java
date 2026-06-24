package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LibLoader {

    private static final Map<String, ClassLoader> loadedLibraries = new ConcurrentHashMap<>();

    public static void loadLibrary(@NotNull Library library) {
        loadLibrary(library, new HashSet<>());
    }

    private static void loadLibrary(@NotNull Library library, @NotNull Set<String> resolved) {
        String key = library.dependency();
        if (loadedLibraries.containsKey(key)) {
            return;
        }
        if (resolved.contains(key)) {
            return;
        }
        resolved.add(key);

        synchronized (LibLoader.class) {
            if (loadedLibraries.containsKey(key)) {
                return;
            }
            try {
                File libDir = new File("libs");
                if (!libDir.exists()) {
                    libDir.mkdirs();
                }

                String fileName = library.artifactId() + "-" + library.version() + ".jar";
                File jarFile = new File(libDir, fileName);

                if (!jarFile.exists()) {
                    downloadJar(library.repository(), library.groupId(), library.artifactId(), library.version(), jarFile);
                }

                downloadTransitiveDependencies(library, libDir, resolved);

                File loadFile = jarFile;
                if (!library.relocate().isEmpty()) {
                    File relocatedFile = new File(libDir, library.artifactId() + "-" + library.version() + "-relocated.jar");
                    if (!relocatedFile.exists()) {
                        BootLoader.relocate(jarFile, relocatedFile, library.relocate());
                    }
                    loadFile = relocatedFile;
                }

                URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{loadFile.toURI().toURL()},
                    LibLoader.class.getClassLoader()
                );
                loadedLibraries.put(key, classLoader);
            } catch (Exception e) {
                throw new RuntimeException("Failed to load library: " + library.dependency(), e);
            }
        }
    }

    private static void downloadTransitiveDependencies(@NotNull Library parent, @NotNull File libDir, @NotNull Set<String> resolved) throws IOException {
        List<PomDependency> dependencies = PomParser.parseDependencies(
            parent.repository(),
            parent.groupId(),
            parent.artifactId(),
            parent.version()
        );

        for (PomDependency dep : dependencies) {
            if (dep.optional()) {
                continue;
            }
            String scope = dep.scope();
            if (!"compile".equals(scope) && !"runtime".equals(scope)) {
                continue;
            }

            String depKey = dep.groupId() + ":" + dep.artifactId();
            if (loadedLibraries.containsKey(depKey) || resolved.contains(depKey)) {
                continue;
            }

            if (dep.version() == null) {
                continue;
            }

            File depJar = new File(libDir, dep.artifactId() + "-" + dep.version() + ".jar");
            if (!depJar.exists()) {
                downloadJar(parent.repository(), dep.groupId(), dep.artifactId(), dep.version(), depJar);
            }

            resolved.add(depKey);

            String depCoord = dep.groupId() + ":" + dep.artifactId() + ":" + dep.version();
            Library transitiveLib = new Library(parent.repository(), depCoord, parent.relocate());
            downloadTransitiveDependencies(transitiveLib, libDir, resolved);

            File loadFile = depJar;
            if (!parent.relocate().isEmpty()) {
                File relocatedFile = new File(libDir, dep.artifactId() + "-" + dep.version() + "-relocated.jar");
                if (!relocatedFile.exists()) {
                    BootLoader.relocate(depJar, relocatedFile, parent.relocate());
                }
                loadFile = relocatedFile;
            }

            URLClassLoader classLoader = new URLClassLoader(
                new URL[]{loadFile.toURI().toURL()},
                LibLoader.class.getClassLoader()
            );
            loadedLibraries.put(depKey, classLoader);
        }
    }

    public static ClassLoader getClassLoader(@NotNull String dependency) {
        return loadedLibraries.get(dependency);
    }

    private static void downloadJar(@NotNull String repository, @NotNull String groupId, @NotNull String artifactId, @NotNull String version, @NotNull File target) throws IOException {
        String repositoryUrl = repository;
        if (!repositoryUrl.endsWith("/")) {
            repositoryUrl += "/";
        }

        String groupIdPath = groupId.replace('.', '/');
        String url = repositoryUrl + groupIdPath + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + ".jar";

        try (InputStream is = new URL(url).openStream()) {
            Files.copy(is, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

}
