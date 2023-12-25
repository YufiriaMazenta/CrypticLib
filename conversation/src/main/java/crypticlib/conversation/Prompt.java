package crypticlib.conversation;

import crypticlib.chat.entry.StringLangConfigEntry;
import net.md_5.bungee.api.chat.BaseComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Prompt {

    @Nullable Prompt acceptInput(Map<Object, Object> conversationData, String input);

    @NotNull BaseComponent promptText(Map<Object, Object> conversationData);

}
