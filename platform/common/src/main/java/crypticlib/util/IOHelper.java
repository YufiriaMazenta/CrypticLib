package crypticlib.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.CopyOption;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IO相关工具类
 */
public class IOHelper {

    public static final Pattern YAML_FILE_PATTERN = Pattern.compile("^(.*)\\.(yaml|yml)$");
    public static final Pattern JSON_FILE_PATTERN = Pattern.compile("^(.*)\\.(json)$");
    public static final Pattern YAML_AND_JSON_FILE_PATTERN = Pattern.compile("^(.*)\\.(yaml|yml|json)$");
    private static final int BUFFER_SIZE = 8192;

    /**
     * 获取文件夹下所有的json文件
     * @param folder 遍历的文件夹
     * @return 文件夹下的json文件
     */
    public static List<File> allJsonFiles(@NotNull File folder) {
        return allFiles(folder, JSON_FILE_PATTERN);
    }

    /**
     * 获取文件夹下所有的Yaml文件
     * @param folder 遍历的文件夹
     * @return 文件夹下的Yaml文件
     */
    public static List<File> allYamlFiles(@NotNull File folder) {
        return allFiles(folder, YAML_FILE_PATTERN);
    }

    /**
     * 获取文件夹下所有文件
     * @param folder 遍历的文件夹
     * @return 文件夹下的文件
     */
    public static List<File> allFiles(@NotNull File folder) {
        return allFiles(folder, null);
    }

    /**
     * 获取一个文件夹下的所有文件名符合条件的文件
     *
     * @param folder          遍历的文件夹
     * @param fileNamePattern 文件名字的过滤条件，当为null时则表示获取所有文件
     * @return 文件夹下所有文件名符合要求的文件，包括其子文件夹的文件
     */
    public static List<File> allFiles(@NotNull File folder, @Nullable Pattern fileNamePattern) {
        List<File> fileList = new ArrayList<>();
        if (folder.isFile() || !folder.exists()) {
            return fileList;
        }
        File[] files = folder.listFiles();
        if (files == null)
            return fileList;
        for (File file : files) {
            if (file.isDirectory()) {
                fileList.addAll(allFiles(file, fileNamePattern));
            } else {
                if (fileNamePattern != null) {
                    Matcher matcher = fileNamePattern.matcher(file.getName());
                    if (matcher.find())
                        fileList.add(file);
                } else {
                    fileList.add(file);
                }
            }
        }
        return fileList;
    }

    /**
     * 是否是yaml文件
     * @param file 文件
     * @return 是否是yaml文件
     */
    public static boolean isYamlFile(@NotNull File file) {
        return YAML_FILE_PATTERN.matcher(file.getName()).find();
    }

    /**
     * 是否是json文件
     * @param file 文件
     */
    public static boolean isJsonFile(@NotNull File file) {
        return JSON_FILE_PATTERN.matcher(file.getName()).find();
    }

    /**
     * 创建一个文件
     *
     * @param file 需要创建的文件
     * @return 是否创建成功
     */
    public static boolean createNewFile(@NotNull File file) {
        try {
            if (!file.getParentFile().exists()) {
                Files.createDirectories(file.getParentFile().toPath());
            }
            Files.createFile(file.toPath());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制文件，底层委托给 Files.copy，但增加了“失败自动清理残骸”的能力。
     *
     * @param source      源文件路径
     * @param target      目标文件路径
     * @param options     复制选项（如 REPLACE_EXISTING, COPY_ATTRIBUTES 等）
     * @return 目标文件路径（即传入的 target）
     * @throws IOException 复制失败时抛出，且会清理不完整的目标文件（特殊场景除外）
     */
    @NotNull
    public static Path copyFile(Path source, Path target, CopyOption... options) throws IOException {
        try {
            // 直接调用 JDK 原生方法，性能最优
            return Files.copy(source, target, options);
        } catch (IOException e) {
            if (!(e instanceof FileAlreadyExistsException)) {
                // 尝试删除可能留下的不完整/空文件。删除失败（如权限不足）则静默忽略，
                try {
                    Files.deleteIfExists(target);
                } catch (IOException ignored) {
                }
            }
            throw e;
        }
    }

    /**
     * 下载文件
     *
     * @param url 地址
     * @param out 目标文件
     */
    public static void downloadFile(URL url, File out) throws IOException {
        Files.createDirectories(out.getParentFile().toPath());
        URLConnection conn = url.openConnection();
        // 显式设置连接/读取超时, 避免不可达仓库无限阻塞启动线程
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(30000);
        conn.setRequestProperty("User-Agent", "CrypticLib");
        try (InputStream ins = conn.getInputStream();
             OutputStream outs = Files.newOutputStream(out.toPath())
        ) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int len;
            while ((len = ins.read(buffer)) > 0) {
                outs.write(buffer, 0, len);
            }
        }
    }

    /**
     * 从URL字符串下载文件
     *
     * @param urlStr URL地址字符串
     * @param out 目标文件
     */
    public static void downloadFile(String urlStr, File out) throws IOException {
        try {
            downloadFile(new URI(urlStr).toURL(), out);
        } catch (Exception e) {
            throw new IOException("Invalid URL: " + urlStr, e);
        }
    }

    /**
     * 读取文件内容
     */
    @Contract("null -> null; !null -> !null")
    public static String readFile(File file) {
        if (file == null)
            return null;
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 从 InputStream 读取全部内容
     *
     * @param inputStream 输入流
     * @param charset     编码
     */
    @NotNull
    public static String readFully(InputStream inputStream, Charset charset) throws IOException {
        return new String(readBytes(inputStream), charset);
    }

    /**
     * 从InputStream中读取全部内容
     *
     * @param inputStream 输入流
     */
    public static byte[] readBytes(InputStream inputStream) throws IOException {
        try(ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                stream.write(buf, 0, len);
            }
            return stream.toByteArray();
        }
    }

    /**
     * 获取某文件相对某文件夹的相对路径名字
     * @param folder 文件夹
     * @param file 文件
     * @return
     */
    public static String getRelativeFileName(@NotNull File folder, @NotNull File file) {
        return getRelativePath(folder, file).toString().replace('\\', '/');
    }

    /**
     * 获取某文件相对某文件夹的相对路径
     * @param folder 文件夹
     * @param file 文件
     * @return
     */
    public static Path getRelativePath(@NotNull File folder, @NotNull File file) {
        try {
            Path folderPath = folder.toPath();
            Path filePath = file.toPath();

            return folderPath.relativize(filePath);
        } catch (IllegalArgumentException e) {
            //路径无效或无法计算相对路径时回退
            return file.toPath();
        }
    }

    /**
     * 校验文件 SHA-1 是否与 .sha1 文件一致
     */
    public static boolean validateSha1(@NotNull File file, @NotNull File sha1File) {
        if (!file.exists() || !sha1File.exists()) {
            return false;
        }
        try {
            String expected = new String(Files.readAllBytes(sha1File.toPath())).trim().split("\\s+")[0];
            String actual = sha1Hex(file);
            return expected.equalsIgnoreCase(actual);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 计算文件的 SHA-1 十六进制摘要
     */
    @NotNull
    public static String sha1Hex(@NotNull File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream is = Files.newInputStream(file.toPath())) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) > 0) {
                digest.update(buf, 0, len);
            }
        }
        byte[] hash = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}
