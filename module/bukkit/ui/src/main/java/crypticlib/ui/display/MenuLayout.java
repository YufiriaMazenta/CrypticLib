package crypticlib.ui.display;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class MenuLayout {

    private List<String> layout;
    private final Map<Character, IconCreator> layoutMap;

    public MenuLayout() {
        this(new ArrayList<>(), new HashMap<>());
    }

    public MenuLayout(@NotNull List<String> layout, @NotNull Supplier<Map<Character, IconCreator>> layoutMapSupplier) {
        this(layout, layoutMapSupplier.get());
    }

    public MenuLayout(@NotNull Supplier<List<String>> layoutSupplier, @NotNull Supplier<Map<Character, IconCreator>> layoutMapSupplier) {
        this(layoutSupplier.get(), layoutMapSupplier.get());
    }

    public MenuLayout(@NotNull List<String> layout, @NotNull Map<Character, IconCreator> layoutMap) {
        this.layout = layout;
        this.layoutMap = new ConcurrentHashMap<>(layoutMap);
    }

    @NotNull
    public List<String> layout() {
        return layout;
    }

    @NotNull
    public MenuLayout setLayout(List<String> layout) {
        this.layout = layout;
        return this;
    }

    /**
     * 添加一种图标
     * @param iconKey 图标的key
     * @param iconCreator 图标的创建器
     * @return
     */
    public MenuLayout addIcon(Character iconKey, IconCreator iconCreator) {
        layoutMap.put(iconKey, iconCreator);
        return this;
    }

    public Optional<IconCreator> getIcon(Character iconKey) {
        return Optional.of(layoutMap.get(iconKey));
    }

    public Optional<IconCreator> removeIcon(Character iconKey) {
        return Optional.of(layoutMap.remove(iconKey));
    }

}
