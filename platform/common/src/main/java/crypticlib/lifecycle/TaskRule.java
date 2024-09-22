package crypticlib.lifecycle;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface TaskRule {

    LifeCycle lifeCycle();

    int priority() default 0;

}
