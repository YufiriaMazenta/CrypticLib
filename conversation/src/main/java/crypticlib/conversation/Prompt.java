package crypticlib.conversation;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public interface Prompt {

    @Nullable Prompt acceptInput(Map<Object, Object> conversationData, String input);

    @NotNull String promptText(Map<Object, Object> conversationData);

}
