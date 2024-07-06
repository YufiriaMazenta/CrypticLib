package crypticlib.lang;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LangHandler {

    String langFileFolder();

    String defLang() default "en_us";

}
