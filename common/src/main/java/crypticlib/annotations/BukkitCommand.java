package crypticlib.annotations;

import java.lang.annotation.*;

/**
 * 用于插件加载时自动注册命令的注解，拥有此注解的命令类将会被自动注册，无需在plugin.yml中声明
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BukkitCommand {

    String name();

    String permission() default "";

    String[] alias() default {};

    String description() default "";

    String usage() default "";

}
