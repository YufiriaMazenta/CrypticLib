package crypticlib.lang;

import crypticlib.lifecycle.Disabler;
import crypticlib.lifecycle.annotation.OnDisable;
import crypticlib.lifecycle.annotation.OnReload;
import crypticlib.lifecycle.Reloader;
import crypticlib.lang.entry.LangEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@OnReload
@OnDisable
public enum LangManager implements Reloader, Disabler {

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
    public void disable() {
        langEntryContainerMap.clear();
        folderLangEntryMap.clear();
    }

    @Override
    public void reload() {
        folderLangEntryMap.clear();
        langEntryContainerMap.forEach(
            (langFolder, container) -> container.reload()
        );
    }

}
