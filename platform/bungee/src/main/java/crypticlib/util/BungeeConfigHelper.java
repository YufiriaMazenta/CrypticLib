package crypticlib.util;

import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

import java.io.IOException;

public class BungeeConfigHelper {

    public static Configuration getBuiltinConfig(String configFilePath) {
        try {
            return ConfigurationProvider.getProvider(YamlConfiguration.class).load(IOHelper.getBuiltinResource(configFilePath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
