package crypticlib.api.command.annotation;

import crypticlib.api.permission.PermissionDefault;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Command {

    String name();
    String permission() default "";
    PermissionDefault permissionDefault() default PermissionDefault.OP;
    String[] aliases() default {};

}
