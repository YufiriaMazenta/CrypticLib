package crypticlib.libloader;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public class JarRelocator {

    private final File input;
    private final File output;
    private final Map<String, String> relocation;

    public JarRelocator(@NotNull File input, @NotNull File output, @NotNull Map<String, String> relocation) {
        this.input = input;
        this.output = output;
        this.relocation = relocation;
    }

    public void run() throws IOException {
        Remapper remapper = new RelocateRemapper(relocation);
        try (JarFile jarFile = new JarFile(input);
             JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(output))) {
            for (JarEntry entry : jarFile.stream().toArray(JarEntry[]::new)) {
                String name = entry.getName();
                String mappedName = remapper.map(name.substring(0, name.length() - 6).replace('/', '.')).replace('.', '/') + ".class";
                jarOutputStream.putNextEntry(new JarEntry(mappedName));
                if (name.endsWith(".class")) {
                    try (InputStream is = jarFile.getInputStream(entry)) {
                        ClassReader classReader = new ClassReader(is);
                        ClassWriter classWriter = new ClassWriter(0);
                        ClassVisitor classVisitor = new ClassRemapper(classWriter, remapper);
                        classReader.accept(classVisitor, 0);
                        jarOutputStream.write(classWriter.toByteArray());
                    }
                }
                jarOutputStream.closeEntry();
            }
        }
    }

    private static class RelocateRemapper extends Remapper {

        private final Map<String, String> relocation;

        public RelocateRemapper(Map<String, String> relocation) {
            this.relocation = relocation;
        }

        @Override
        public String map(String internalName) {
            for (Map.Entry<String, String> entry : relocation.entrySet()) {
                String oldPrefix = entry.getKey().replace('.', '/');
                String newPrefix = entry.getValue().replace('.', '/');
                if (internalName.startsWith(oldPrefix)) {
                    return newPrefix + internalName.substring(oldPrefix.length());
                }
            }
            return internalName;
        }

    }

}
