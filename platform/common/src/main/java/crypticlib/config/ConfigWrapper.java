package crypticlib.config;

import crypticlib.CrypticLib;
import crypticlib.util.IOHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 对配置文件的封装，支持yaml
 */
public abstract class ConfigWrapper<C> {

    protected final File configFile;
    protected final String path;
    //volatile保证reloadConfig替换引用后对其他线程立即可见
    protected volatile C config;
    //用于同步锁
    protected final Object lock = new Object();

    /**
     * 从指定插件中释放并创建一个配置文件
     *
     * @param path   相对插件文件夹的路径
     */
    public ConfigWrapper(@NotNull File dataFolder, @NotNull String path) {
        this.path = path;
        this.configFile = new File(dataFolder, path);
        reloadConfig();
    }

    /**
     * 从指定的File对象中创建一个配置文件
     *
     * @param file 创建的配置文件
     */
    public ConfigWrapper(@NotNull File file) {
        this.configFile = file;
        this.path = file.getPath();
        reloadConfig();
    }

    /**
     * 获取配置文件实例
     *
     * @return 配置文件实例
     */
    @NotNull
    public C config() {
        if (config == null) {
            synchronized (lock) {
                if (config == null) {
                    reloadConfig();
                }
            }
        }
        return config;
    }

    public abstract boolean contains(String key);

    /**
     * 设置配置文件指定路径的值
     *
     * @param key    配置的路径
     * @param object 值
     */
    public abstract void set(@NotNull String key, @Nullable Object object);

    public abstract void setComments(@NotNull String key, @Nullable List<String> comments);

    public abstract @Nullable List<String> getComments(@NotNull String key);

    /**
     * 重载配置文件
     */
    public abstract void reloadConfig();

    /**
     * 保存配置文件
     */
    public abstract void saveConfig();

    public void saveDefaultConfigFile() {
        synchronized (lock) {
            if (!configFile.exists()) {
                //存在同名.broken时拒绝释放默认配置: 源文件是被上一次解析失败移走的,
                //此时若静默释放默认配置, 插件会带着一份默认值正常启动,
                //管理员可能没注意到报错, 误以为自己的配置已生效, 实际所有自定义值都丢了
                File broken = brokenConfigFile();
                if (broken.exists()) {
                    throw new IllegalStateException(
                        "Refusing to write default config for " + configFile
                            + " because a previous parse failure left " + broken.getName()
                            + "; fix and rename it back, or delete it to start from defaults");
                }
                try (InputStream input = getResource(path)) {
                    IOHelper.createNewFile(configFile);
                    if (input == null) {
                        return;
                    }
                    Files.write(configFile.toPath(), IOHelper.readBytes(input));
                } catch (IOException e) {
                    if (CrypticLib.debug) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /**
     * 返回解析失败时用于隔离原文件的.broken文件
     *
     * @return 与配置文件同目录的同名.broken文件
     */
    @NotNull
    protected File brokenConfigFile() {
        return new File(configFile.getAbsolutePath() + ".broken");
    }

    /**
     * 解析失败时把原文件移走并另存为.broken, 供子类在解析异常分支中调用
     * <p>
     * 使用移动而非复制: 坏文件不会留在原位反复触发解析失败,
     * 同时由{@link #saveDefaultConfigFile()}的.broken检查保证下次重载不会静默降级为默认配置。
     * 已存在的.broken会被覆盖, 即只保留最近一次的坏文件。
     */
    protected void backupBrokenConfigFile() {
        synchronized (lock) {
            try {
                Files.move(configFile.toPath(), brokenConfigFile().toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * 返回配置文件相对于插件文件夹的路径
     *
     * @return 配置文件的路径
     */
    @NotNull
    public String filePath() {
        return path;
    }

    @NotNull
    public File configFile() {
        return configFile;
    }

    protected @Nullable InputStream getResource(@NotNull String filename) {
        try {
            URL url = this.getClass().getClassLoader().getResource(filename);
            if (url == null) {
                return null;
            } else {
                URLConnection connection = url.openConnection();
                connection.setUseCaches(false);
                return connection.getInputStream();
            }
        } catch (IOException var4) {
            return null;
        }
    }

    /**
     * 删除这个配置文件所对应的文件,如果删除失败,则会打印错误消息
     */
    public boolean deleteConfigFile() {
        synchronized (lock) {
            try {
                Files.delete(this.configFile.toPath());
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
    }

}
