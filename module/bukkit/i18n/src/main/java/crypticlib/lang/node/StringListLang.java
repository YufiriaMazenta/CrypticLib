package crypticlib.lang.node;

import crypticlib.BukkitInvoker;
import crypticlib.lang.LangContainer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
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
            //配置对象只取一次, 避免每个键都从包装类中重新取出
            YamlConfiguration config = configWrapper.config();
            //键缺失时不能存入空列表, 否则该语言会顶掉默认语言的翻译
            if (config.contains(key)) {
                langMap.put(lang, config.getStringList(key));
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
