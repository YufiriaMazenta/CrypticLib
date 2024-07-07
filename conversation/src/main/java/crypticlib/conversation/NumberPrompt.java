package crypticlib.conversation;

import crypticlib.util.StringHelper;

import java.util.Map;

public interface NumberPrompt extends ValidatingPrompt {

    @Override
    default boolean isInputValid(String input) {
        return StringHelper.isNumber(input);
    }

    @Override
    default Prompt acceptValidatedInput(Map<Object, Object> conversationData, String input) {
        if (isInputValid(input)) {
            return acceptValidatedInput(conversationData, StringHelper.toNumber(input));
        } else {
            return this;
        }
    }

    Prompt acceptValidatedInput(Map<Object, Object> conversationData, Number input);

}
