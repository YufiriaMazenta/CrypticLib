package crypticlib.conversation;

import java.util.Map;

public interface ValidatingPrompt extends Prompt {

    @Override
    default Prompt acceptInput(Map<Object, Object> conversationData, String input) {
        if (isInputValid(input)) {
            return acceptValidatedInput(conversationData, input);
        } else {
            return this;
        }
    }

    boolean isInputValid(String input);

    Prompt acceptValidatedInput(Map<Object, Object> conversationData, String input);

}
