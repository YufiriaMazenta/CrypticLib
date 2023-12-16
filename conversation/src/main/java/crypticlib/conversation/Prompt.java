package crypticlib.conversation;

import crypticlib.chat.entry.StringLangConfigEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Prompt {

    @Nullable Prompt acceptInput(Map<Object, Object> conversationData, String input);

    @NotNull StringLangConfigEntry promptText(Map<Object, Object> conversationData);

}
