package crypticlib.lang;

import crypticlib.internal.PluginScanner;
import crypticlib.lang.entry.LangEntry;
import crypticlib.lifecycle.AutoTask;
import crypticlib.lifecycle.BukkitLifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@AutoTask(
    lifecycles = {LifeCycle.LOAD, LifeCycle.RELOAD, LifeCycle.DISABLE}
)
public enum LangManager implements BukkitLifeCycleTask {

    INSTANCE;
    private final Map<String, Map<String, LangEntry<?>>> folderLangEntryMap = new ConcurrentHashMap<>();
    private final Map<String, LangEntryContainer> langEntryContainerMap = new ConcurrentHashMap<>();

    public void loadLangEntryContainer(@NotNull String langFileFolder, @NotNull LangEntryContainer container) {
        langEntryContainerMap.put(langFileFolder, container);
        container.reload();
    }

    public void removeLangEntryContainer(@NotNull String langFileFolder) {
        LangEntryContainer container = langEntryContainerMap.remove(langFileFolder);
        if (container == null)
            return;
        folderLangEntryMap.remove(container.langFileFolder());
    }

    @Nullable
    public LangEntry<?> getLangEntry(@NotNull String langFileFolder, @NotNull String key) {
        Map<String, LangEntry<?>> langEntryMap = folderLangEntryMap.get(langFileFolder);
        if (langEntryMap == null)
            return null;
        return langEntryMap.get(key);
    }

    public void putLangEntry(@NotNull String langFileFolder, @NotNull LangEntry<?> entry) {
        Map<String, LangEntry<?>> langEntryMap;
        if (folderLangEntryMap.containsKey(langFileFolder)) {
            langEntryMap = folderLangEntryMap.get(langFileFolder);
        } else {
            langEntryMap = new ConcurrentHashMap<>();
            folderLangEntryMap.put(langFileFolder, langEntryMap);
        }
        langEntryMap.put(entry.key(), entry);
    }

    @Override
    public void run(Plugin plugin, LifeCycle lifeCycle) {
        switch (lifeCycle) {
            case LOAD:
                PluginScanner.INSTANCE.getAnnotatedClasses(LangHandler.class).forEach(
                    langClass -> {
                        LangHandler langHandler = langClass.getAnnotation(LangHandler.class);
                        String langFileFolder = langHandler.langFileFolder();
                        String defLang = langHandler.defLang();
                        LangEntryContainer langEntryContainer = new LangEntryContainer(plugin, langClass, langFileFolder, defLang);
                        LangManager.INSTANCE.loadLangEntryContainer(langFileFolder, langEntryContainer);
                    }
                );
                break;
            case RELOAD:
                folderLangEntryMap.clear();
                langEntryContainerMap.forEach(
                    (langFolder, container) -> container.reload()
                );
                break;
            case DISABLE:
                langEntryContainerMap.clear();
                folderLangEntryMap.clear();
                break;
        }
    }
}
