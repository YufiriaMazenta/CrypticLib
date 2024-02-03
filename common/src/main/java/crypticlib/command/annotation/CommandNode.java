package crypticlib.command.annotation;

import java.lang.annotation.*;

/**
 * 作用在属性上的子命令注解，必须使用在BiFunction<CommandSender, List<String>, Boolean>类型的变量上，否则可能会造成严重后果
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandNode { }
