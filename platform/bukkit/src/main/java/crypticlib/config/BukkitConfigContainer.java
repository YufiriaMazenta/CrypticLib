package crypticlib.config;

import crypticlib.config.node.BukkitConfigNode;
import crypticlib.util.ReflectionHelper;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class BukkitConfigContainer extends ConfigContainer<BukkitConfigWrapper> {

    public BukkitConfigContainer(@NotNull Class<?> containerClass, @NotNull BukkitConfigWrapper configWrapper) {
        super(containerClass, configWrapper);
    }

    @Override
    public void reload() {
//        configWrapper.reloadConfig(); 不再由ConfigContainer进行重载
        //仅在确实补写了默认值(存在缺失的键)时才保存, 避免Bukkit<1.18.1每次reload无条件重写文件抹掉注释
        boolean changed = false;
        for (Class<?> c = containerClass; c != null; c = c.getSuperclass()) {
        for (Field field : c.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()))
                continue;
            Object obj;
            try {
                obj = ReflectionHelper.getDeclaredFieldObj(field, null);
            } catch (IllegalAccessException e) {
                continue;
            }
            if (obj instanceof BukkitConfigNode<?>) {
                BukkitConfigNode<?> config = (BukkitConfigNode<?>) obj;
                if (config.configContainer() == null)
                    config.setConfigContainer(this);
                if (!configWrapper.config().contains(config.key()))
                    changed = true;
                config.saveDef(configWrapper.config());
                //单个节点加载异常时记录告警并跳过, 不让一个坏键中断整个配置类的加载
                try {
                    config.load(configWrapper.config());
                } catch (Throwable t) {
                    Bukkit.getLogger().warning("Failed to load config value at '" + config.key() + "' in "
                        + configWrapper.configFile().getName() + ": " + t.getMessage());
                }
            }
        }
        }
        if (changed) {
            configWrapper.saveConfig();
        }
    }

    @Override
    public @NotNull BukkitConfigWrapper configWrapper() {
        return super.configWrapper();
    }
}
