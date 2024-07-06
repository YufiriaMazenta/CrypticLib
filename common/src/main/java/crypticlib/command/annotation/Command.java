package crypticlib.command.annotation;

import java.lang.annotation.*;

/**
 * 用于标识命令的注解，拥有此注解的命令树将会被自动注册为插件命令，无需在plugin.yml中声明
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Command { }
