package crypticlib.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class VelocityTextProcessor {

    private static final LegacyComponentSerializer SERIALIZER = LegacyComponentSerializer.builder().character('&').hexColors().hexCharacter('#').build();

    public static Component toComponent(String text) {
        return SERIALIZER.deserialize(text);
    }

}
