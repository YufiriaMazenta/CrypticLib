package crypticlib.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.*;

/**
 * 用于插件加载时自动注册命令的注解，拥有此注解的命令类将会被自动注册，无需在plugin.yml中声明
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BukkitCommand {

    /**
     * 命令的名字
     *
     * @return 命令的名字, 不能为空
     */
    @NotNull String name();

    /**
     * 命令所需的权限
     *
     * @return 命令所需的权限
     */
    @Nullable String permission() default "";

    /**
     * 1
     * 命令的别名,空即为无别名
     *
     * @return 命令的别名
     */
    @NotNull String[] aliases() default {};

    /**
     * 命令的介绍
     *
     * @return 命令的介绍
     */
    @NotNull String description() default "";

    /**
     * 命令的提示文本
     *
     * @return 命令的提示文本
     */
    @NotNull String usage() default "";

}
