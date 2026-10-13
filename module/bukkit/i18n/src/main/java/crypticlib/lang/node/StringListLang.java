package crypticlib.lang.node;

import crypticlib.BukkitInvoker;
import crypticlib.lang.LangContainer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class StringListLang extends LangNode<List<String>> {

    public StringListLang(@NotNull String key) {
        this(key, Collections.singletonList(key));
    }

    public StringListLang(@NotNull String key, List<String> def) {
        super(key, def);
    }

    @Override
    public StringListLang load(LangContainer configContainer) {
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
            BukkitInvoker.byCommandSender(sender).sendMsg(str, replaceMap);
        }
    }

}
