package crypticlib.config;

import crypticlib.PlatformSide;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigHandler {

    @NotNull String path();

    @NotNull PlatformSide[] platforms() default {PlatformSide.BUKKIT, PlatformSide.BUNGEE, PlatformSide.VELOCITY};

}
