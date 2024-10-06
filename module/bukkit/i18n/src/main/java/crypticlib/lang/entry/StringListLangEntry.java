package crypticlib.lang.entry;

import crypticlib.chat.BukkitMsgSender;
import crypticlib.lang.LangEntryContainer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StringListLangEntry extends LangEntry<List<String>> {

    public StringListLangEntry(@NotNull String key) {
        this(key, Collections.singletonList(key));
    }

    public StringListLangEntry(@NotNull String key, List<String> def) {
        super(key, def);
    }

    @Override
    public StringListLangEntry load(LangEntryContainer configContainer) {
        langMap.clear();
        configContainer.langConfigWrapperMap().forEach((lang, configWrapper) -> {
            if (configWrapper.contains(key)) {
                langMap.put(lang, configWrapper.config().getStringList(key));
            }
        });
        return this;
    }

    @Override
    public void send(CommandSender sender, Map<String, String> replaceMap) {
        List<String> lang;
        if (sender instanceof Player) {
            lang = value((Player) sender);
        } else {
            lang = value();
        }
        for (String str : lang) {
            BukkitMsgSender.INSTANCE.sendMsg(sender, str, replaceMap);
        }
    }

}
