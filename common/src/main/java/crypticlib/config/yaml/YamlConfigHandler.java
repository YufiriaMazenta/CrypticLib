package crypticlib.config.yaml;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface YamlConfigHandler {

    @NotNull String path();

}
