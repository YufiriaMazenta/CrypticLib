package crypticlib.libloader;

import crypticlib.CrypticLib;
import crypticlib.util.IOHelper;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
                File libDir = getJarDir(library.groupId(), library.artifactId(), library.version());
                if (!libDir.exists()) {
                    libDir.mkdirs();
                }

                String fileName = library.artifactId() + "-" + library.version() + ".jar";
                File jarFile = new File(libDir, fileName);

                if (!jarFile.exists()) {
                    IOHelper.info("Downloading " + library.groupId() + ":" + library.artifactId() + ":" + library.version() + " from " + library.repository());
                    downloadJar(library.repository(), library.groupId(), library.artifactId(), library.version(), jarFile);
                }

                downloadTransitiveDependencies(library, resolved);

                File loadFile = jarFile;
                if (!library.relocate().isEmpty()) {
                    File relocatedFile = new File(libDir, library.artifactId() + "-" + library.version() + "-relocated.jar");
                    if (!relocatedFile.exists()) {
                        IOHelper.info("Relocating " + library.dependency() + " to " + library.relocate());
                        BootLoader.relocate(jarFile, relocatedFile, library.relocate());
                    }
                    loadFile = relocatedFile;
                }

                URLClassLoader classLoader = new URLClassLoader(
                    new URL[]{loadFile.toURI().toURL()},
                    LibLoader.class.getClassLoader()
                );
                verifyClassLoader(classLoader, library.groupId(), library.artifactId());
                loadedLibraries.put(key, classLoader);
                IOHelper.info("Successfully loaded " + library.dependency());
            } catch (Exception e) {
                IOHelper.info("Failed to load " + library.dependency() + ": " + e.getMessage());
                throw new RuntimeException("Failed to load library: " + library.dependency(), e);
            }
        }
    }

    private static void downloadTransitiveDependencies(@NotNull Library parent, @NotNull Set<String> resolved) throws IOException {
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

            File depDir = getJarDir(dep.groupId(), dep.artifactId(), dep.version());
            if (!depDir.exists()) {
                depDir.mkdirs();
            }

            File depJar = new File(depDir, dep.artifactId() + "-" + dep.version() + ".jar");
            if (!depJar.exists()) {
                IOHelper.info("Downloading transitive dependency " + dep.groupId() + ":" + dep.artifactId() + ":" + dep.version());
                downloadJar(parent.repository(), dep.groupId(), dep.artifactId(), dep.version(), depJar);
            }

            resolved.add(depKey);

            String depCoord = dep.groupId() + ":" + dep.artifactId() + ":" + dep.version();
            Library transitiveLib = new Library(parent.repository(), depCoord, parent.relocate());
            downloadTransitiveDependencies(transitiveLib, resolved);

            File loadFile = depJar;
            if (!parent.relocate().isEmpty()) {
                File relocatedFile = new File(depDir, dep.artifactId() + "-" + dep.version() + "-relocated.jar");
                if (!relocatedFile.exists()) {
                    IOHelper.info("Relocating transitive dependency " + dep.groupId() + ":" + dep.artifactId());
                    BootLoader.relocate(depJar, relocatedFile, parent.relocate());
                }
                loadFile = relocatedFile;
            }

            URLClassLoader classLoader = new URLClassLoader(
                new URL[]{loadFile.toURI().toURL()},
                LibLoader.class.getClassLoader()
            );
            verifyClassLoader(classLoader, dep.groupId(), dep.artifactId());
            loadedLibraries.put(depKey, classLoader);
            IOHelper.info("Successfully loaded transitive dependency " + depKey);
        }
    }

    @NotNull
    private static File getJarDir(@NotNull String groupId, @NotNull String artifactId, @NotNull String version) {
        String pluginDir = "plugins/" + CrypticLib.pluginName() + "/libs";
        String groupIdPath = groupId.replace('.', '/');
        return new File(pluginDir + "/" + groupIdPath + "/" + artifactId + "/" + version);
    }

    public static ClassLoader getClassLoader(@NotNull String dependency) {
        return loadedLibraries.get(dependency);
    }

    private static void verifyClassLoader(@NotNull URLClassLoader classLoader, @NotNull String groupId, @NotNull String artifactId) throws IOException {
        File jarFile;
        try {
            jarFile = new File(classLoader.getURLs()[0].toURI());
        } catch (URISyntaxException e) {
            throw new IOException("Invalid jar URI", e);
        }
        try (JarFile jar = new JarFile(jarFile)) {
            boolean hasClass = false;
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class") && !entry.isDirectory()) {
                    hasClass = true;
                    break;
                }
            }
            if (!hasClass) {
                throw new IOException("Jar file contains no classes: " + groupId + ":" + artifactId);
            }
        }
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