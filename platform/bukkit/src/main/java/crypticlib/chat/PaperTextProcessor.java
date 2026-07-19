package crypticlib.chat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

/**
 * 提供paper端特有的一些文本处理
 * 主要是AdventureAPI的方法
 */
public class PaperTextProcessor {

    private static final LegacyComponentSerializer LEGACY_SERIALIZER = LegacyComponentSerializer
        .builder()
        .character('&')
        .hexColors()
        .hexCharacter('#')
        .build();
    private static final PlainTextComponentSerializer PLAIN_SERIALIZER = PlainTextComponentSerializer
        .builder()
        .build();
    private static final MiniMessage MINI_MESSAGE_SERIALIZER = MiniMessage.miniMessage();

    @Contract("null -> null")
    public static @Nullable String serializeLegacyText(Component component) {
        if (component == null) {
            return null;
        }
        return LEGACY_SERIALIZER.serialize(component);
    }

    @Contract("null -> null")
    public static @Nullable Component deserializeLegacyText(String text) {
        if (text == null) {
            return null;
        }
        return LEGACY_SERIALIZER.deserialize(text);
    }

    @Contract("null -> null")
    public static @Nullable String serializePlainText(Component component) {
        if (component == null) {
            return null;
        }
        return PLAIN_SERIALIZER.serialize(component);
    }

    @Contract("null -> null")
    public static Component deserializePlainText(String text) {
        if (text == null) {
            return null;
        }
        return PLAIN_SERIALIZER.deserialize(text);
    }

    @Contract("null -> null")
    public static Component deserializeMiniMessage(String text) {
        if (text == null) {
            return null;
        }
        return MINI_MESSAGE_SERIALIZER.deserialize(text);
    }

    @Contract("null -> null")
    public static String serializeMiniMessage(Component component) {
        if (component == null) {
            return null;
        }
        return MINI_MESSAGE_SERIALIZER.serialize(component);
    }

}
