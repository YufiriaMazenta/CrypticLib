package crypticlib.config;

import crypticlib.config.node.BungeeConfigNode;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class BungeeConfigContainer extends ConfigContainer<BungeeConfigWrapper> {

    public BungeeConfigContainer(@NotNull Class<?> containerClass, @NotNull BungeeConfigWrapper configWrapper) {
        super(containerClass, configWrapper);
    }

    @Override
    public void reload() {
//        configWrapper.reloadConfig(); 不再由ConfigContainer进行重载
        //Bungee的Configuration不支持注释, 每次saveConfig都会按内存内容重新dump并抹掉用户注释,
        //因此仅在确实补写了默认值(存在缺失的键)时才保存, 避免无谓地整文件重写
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
            if (obj instanceof BungeeConfigNode<?>) {
                BungeeConfigNode<?> config = (BungeeConfigNode<?>) obj;
                if (config.configContainer() == null)
                    config.setConfigContainer(this);
                if (!configWrapper.config().contains(config.key()))
                    changed = true;
                config.saveDef(configWrapper.config());
                config.load(configWrapper.config());
            }
        }
        }
        if (changed) {
            configWrapper.saveConfig();
        }
    }

    @Override
    public @NotNull BungeeConfigWrapper configWrapper() {
        return super.configWrapper();
    }
}
