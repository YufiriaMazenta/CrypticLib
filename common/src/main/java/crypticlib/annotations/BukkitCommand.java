package crypticlib.annotations;

import java.lang.annotation.*;

/**
 * 用于插件加载时自动注册命令的注解，拥有此注解的命令类将会被自动注册，无需在plugin.yml中声明
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BukkitCommand {

    boolean reg() default true;

    /**
     * 命令的名字
     * @return 命令的名字,不能为空
     */
    String name();

    /**
     * 命令所需的权限,空字符串即为无权限
     * @return 命令所需的权限
     */
    String permission() default "";

    /**
     * 命令的别名,空即为无别名
     * @return 命令的别名
     */
    String[] alias() default {};

    /**
     * 命令的介绍
     * @return 命令的介绍
     */
    String description() default "";

    /**
     * 命令的提示文本
     * @return 命令的提示文本
     */
    String usage() default "";

}
