package crypticlib.config;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ConfigHandler {

    @NotNull String path();

}
