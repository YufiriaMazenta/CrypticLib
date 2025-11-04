package crypticlib.resource;

import java.lang.annotation.*;

/**
 * 用于标记一组在线资源的注解
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NetworkResources {

    NetworkResource[] resources();

}
