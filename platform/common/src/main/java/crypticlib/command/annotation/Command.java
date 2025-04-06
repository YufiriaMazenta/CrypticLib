package crypticlib.command.annotation;

import crypticlib.PlatformSide;

import java.lang.annotation.*;

/**
 * 用于标识命令的注解，拥有此注解的命令树将会被自动注册为插件命令，无需在plugin.yml中声明
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Command {

    /**
     * 新建实例时是否无视ClassNotFoundException和NoClassDefFoundError
     * 若为true,遇到上述异常将不会打印报错
     * 若为false,将会打印报错堆栈信息
     */
    boolean ignoreClassNotFound() default true;

    /**
     * 命令所属的平台，若不包含当前平台，将不会注册命令
     */
    PlatformSide[] platforms() default {PlatformSide.BUKKIT, PlatformSide.BUNGEE, PlatformSide.VELOCITY};

}
