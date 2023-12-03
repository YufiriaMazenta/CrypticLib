package crypticlib.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文件相关工具类
 */
public class FileUtil {

    public static final Pattern YAML_FILE_PATTERN = Pattern.compile("^(.*)\\.(yaml|yml)$");
    public static final Pattern JSON_FILE_PATTERN = Pattern.compile("^(.*)\\.(json)$");
    public static final Pattern YAML_AND_JSON_FILE_PATTERN = Pattern.compile("^(.*)\\.(json)$");

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

}
