package crypticlib.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUtil {

    /**
     * 获取一个文件夹下的所有文件名符合条件的文件
     * @param folder 遍历的文件夹
     * @param fileNamePattern 文件名字的过滤条件，当为null时则表示获取所有文件
     * @return 文件夹下所有文件名符合要求的文件，包括其子文件夹的文件
     */
    public static List<File> allFiles(File folder, Pattern fileNamePattern) {
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
     * @param file 需要创建的文件
     * @return 是否创建成功
     */
    public static boolean createNewFile(File file) {
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
