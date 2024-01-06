package crypticlib.command;

import crypticlib.perm.PermDef;
import org.jetbrains.annotations.NotNull;

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
    String permission() default "";

    /**
     * 命令权限的默认值
     * @return 命令权限的默认值，将会影响玩家是否默认拥有此权限
     */
    PermDef permDef() default PermDef.FALSE;

    /**
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
