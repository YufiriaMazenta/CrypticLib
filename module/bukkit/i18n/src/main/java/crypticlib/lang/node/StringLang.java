package crypticlib.lang.node;

import crypticlib.BukkitInvoker;
import crypticlib.BukkitPlayer;
import crypticlib.lang.LangContainer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class StringLang extends LangNode<String> {

    public StringLang(@NotNull String key) {
        this(key, key);
    }

    public StringLang(@NotNull String key, String def) {
        super(key, def);
    }

    @Override
    public StringLang load(LangContainer configContainer) {
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
            BukkitPlayer.byPlayer(bukkitPlayer).sendMsg(value(bukkitPlayer), replaceMap);
        } else {
            BukkitInvoker.byCommandSender(sender).sendMsg(value(), replaceMap);
        }
    }

}
