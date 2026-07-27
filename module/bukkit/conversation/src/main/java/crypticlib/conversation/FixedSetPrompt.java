package crypticlib.conversation;

import java.util.Set;

public interface FixedSetPrompt extends ValidatingPrompt {

    @Override
    default boolean isInputValid(String input) {
        String lower = input.toLowerCase();
        for (String s : fixedSet()) {
            if (s.equalsIgnoreCase(lower)) {
                return true;
            }
        }
        return false;
    }

    Set<String> fixedSet();

}
