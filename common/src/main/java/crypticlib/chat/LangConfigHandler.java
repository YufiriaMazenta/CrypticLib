package crypticlib.chat;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LangConfigHandler {

    String langFileFolder();

    String defLang() default "en_us";

}
