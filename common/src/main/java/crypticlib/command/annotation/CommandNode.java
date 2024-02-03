package crypticlib.command.annotation;

import java.lang.annotation.*;

/**
 * 用于标识命令树节点的注解，必须使用在CommandTreeNode类型的变量上
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CommandNode { }
