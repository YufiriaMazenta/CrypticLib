package crypticlib.conversation;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;

public interface PlayerPrompt extends ValidatingPrompt {

    @Override
    default boolean isInputValid(String input) {
        return Bukkit.getPlayer(input) != null;
    }

    @Override
    default Prompt acceptValidatedInput(Map<Object, Object> conversationData, String input) {
        if (isInputValid(input)) {
            return acceptValidatedInput(conversationData, Bukkit.getPlayer(input));
        } else {
            return this;
        }
    }

    Prompt acceptValidatedInput(Map<Object, Object> conversationData, Player player);

}
