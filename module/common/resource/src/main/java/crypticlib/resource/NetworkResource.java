package crypticlib.resource;

import java.lang.annotation.*;

/**
 * 用于标记在线资源的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NetworkResource {

    /**
     * 下载链接,将会按照顺序尝试下载,如果全部下载失败且{@link NetworkResource#throwIfFailed()}为true,将会卸载插件,否则打印报错
     */
    String[] downloadUrl();

    /**
     * 用于存放下载文件的路径,基于插件数据文件夹
     */
    String filePath();

    /**
     * 若此资源加载失败,是否卸载插件
     */
    boolean throwIfFailed() default false;

    /**
     * 如果文件已经存在,是否下载替换
     */
    boolean downloadIfExist() default false;

}
