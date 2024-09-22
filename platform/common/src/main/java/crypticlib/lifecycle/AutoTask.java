package crypticlib.lifecycle;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoTask {

    TaskRule[] rules();

}
