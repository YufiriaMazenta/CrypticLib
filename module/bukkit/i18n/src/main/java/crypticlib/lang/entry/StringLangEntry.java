package crypticlib.lang.entry;

import crypticlib.command.BukkitCommandInvoker;
import crypticlib.BukkitPlayer;
import crypticlib.lang.LangEntryContainer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StringLangEntry extends LangEntry<String> {

    public StringLangEntry(@NotNull String key) {
        this(key, key);
    }

    public StringLangEntry(@NotNull String key, String def) {
        super(key, def);
    }

    @Override
    public StringLangEntry load(LangEntryContainer configContainer) {
        langMap.clear();
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            if (configWrapper.contains(key)) {
                langMap.put(lang, configWrapper.config().getString(key));
            }
        });
        return this;
    }

    @Override
    public void send(CommandSender sender, Map<String, String> replaceMap) {
        if (sender instanceof Player) {
            Player bukkitPlayer = (Player) sender;
            new BukkitPlayer(bukkitPlayer).sendMsg(value(bukkitPlayer), replaceMap);
        } else {
            new BukkitCommandInvoker(sender).sendMsg(value(), replaceMap);
        }
    }

}
