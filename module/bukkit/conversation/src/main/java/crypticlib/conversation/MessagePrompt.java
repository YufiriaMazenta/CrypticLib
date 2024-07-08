package crypticlib.conversation;

import java.util.Map;

public interface MessagePrompt extends Prompt {

    @Override
    default Prompt acceptInput(Map<Object, Object> conversationData, String input) {
        return nextPrompt(conversationData);
    }

    Prompt nextPrompt(Map<Object, Object> conversationData);

}
