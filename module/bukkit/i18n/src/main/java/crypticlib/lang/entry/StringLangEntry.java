package crypticlib.lang.entry;

import crypticlib.chat.BukkitMsgSender;
import crypticlib.lang.LangEntryContainer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
    public void send(CommandSender sender) {
        if (sender instanceof Player) {
            BukkitMsgSender.INSTANCE.sendMsg(sender, value((Player) sender));
        } else {
            BukkitMsgSender.INSTANCE.sendMsg(sender, value());
        }
    }

}
