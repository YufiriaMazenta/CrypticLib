package crypticlib.internal.annotation;

import crypticlib.internal.Platform;

import java.lang.annotation.*;

/**
 * 用于标注此类是否在此平台实现的注解，若用于标注主类，
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PlatformSide {

    Platform[] platform();

}
