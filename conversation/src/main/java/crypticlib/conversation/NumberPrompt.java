package crypticlib.conversation;

import crypticlib.util.StringUtil;

import java.util.Map;

public interface NumberPrompt extends ValidatingPrompt {

    @Override
    default boolean isInputValid(String input) {
        return StringUtil.isNumber(input);
    }

    @Override
    default Prompt acceptValidatedInput(Map<Object, Object> conversationData, String input) {
        if (isInputValid(input)) {
            return acceptValidatedInput(conversationData, StringUtil.toNumber(input));
        } else {
            return this;
        }
    }

    Prompt acceptValidatedInput(Map<Object, Object> conversationData, Number input);

}
