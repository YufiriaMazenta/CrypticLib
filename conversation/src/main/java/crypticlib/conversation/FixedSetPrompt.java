package crypticlib.conversation;

import java.util.Set;

public interface FixedSetPrompt extends ValidatingPrompt {

    @Override
    default boolean isInputValid(String input) {
        return fixedSet().contains(input.toLowerCase());
    }

    Set<String> fixedSet();

}
