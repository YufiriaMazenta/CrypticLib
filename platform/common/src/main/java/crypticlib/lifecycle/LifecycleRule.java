package crypticlib.lifecycle;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LifecycleRule {

    Lifecycle lifeCycle();

    int priority() default 0;

}
