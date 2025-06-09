package crypticlib.util;

import crypticlib.chat.MsgSender;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
    private static MsgSender<?, ?, ?> msgSender;

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
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            return file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 复制文件
     *
     * @param from 原始文件
     * @param to 复制结果
     */
    @NotNull
    public static File copyFile(File from, File to) {
        try (
            FileInputStream fileIn = new FileInputStream(from);
            FileOutputStream fileOut = new FileOutputStream(to);
            FileChannel channelIn = fileIn.getChannel();
            FileChannel channelOut = fileOut.getChannel()
        ) {
            channelIn.transferTo(0, channelIn.size(), channelOut);
        } catch (IOException t) {
            t.printStackTrace();
        }
        return to;
    }

    /**
     * 下载文件
     *
     * @param url 地址
     * @param out 目标文件
     */
    @SuppressWarnings("StatementWithEmptyBody")
    public static void downloadFile(URL url, File out) throws IOException {
        out.getParentFile().mkdirs();
        InputStream ins = url.openStream();
        OutputStream outs = Files.newOutputStream(out.toPath());
        byte[] buffer = new byte[BUFFER_SIZE];
        for (int len; (len = ins.read(buffer)) > 0; outs.write(buffer, 0, len)) ;
        outs.close();
        ins.close();
    }

    /**
     * 读取文件内容
     */
    @Contract("null -> null; !null -> !null")
    public static String readFile(File file) {
        if (file == null)
            return null;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            return readFully(fileInputStream, StandardCharsets.UTF_8);
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
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        byte[] buf = new byte[BUFFER_SIZE];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            stream.write(buf, 0, len);
        }
        return stream.toByteArray();
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码
     *
     * @param msg 发送的文本
     */
    public static void info(String msg) {
        msgSender.info(msg);
    }

    /**
     * 给控制台发送一条文本，此文本会处理颜色代码，并根据replaceMap的内容替换源文本
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void info(String msg, Map<String, String> replacements) {
        msgSender.info(msg, replacements);
    }

    /**
     * 向后台发送一条DEBUG文本
     *
     * @param msg        发送的文本
     */
    public static void debug(String msg) {
        msgSender.debug(msg);
    }

    /**
     * 向后台发送一条DEBUG文本
     *
     * @param msg        发送的文本
     * @param replacements 需要替换的文本
     */
    public static void debug(String msg, Map<String, String> replacements) {
        msgSender.debug(msg, replacements);
    }

    public static void setMsgSender(MsgSender<?, ?, ?> msgSender) {
        if (IOHelper.msgSender != null) {
            throw new UnsupportedOperationException("MsgSender is already set");
        }
        IOHelper.msgSender = msgSender;
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

}
