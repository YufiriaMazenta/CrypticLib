package crypticlib.command.annotation;

import java.lang.annotation.*;

/**
 * 用于标识子命令的注解，必须使用在Subcommand的实现类上，否则无效
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Subcommand { }
