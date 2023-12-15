package crypticlib.ui.display;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Supplier;

public class MenuLayout {

    private List<String> layout;
    private Map<Character, Icon> layoutMap;

    public MenuLayout() {
        this(new ArrayList<>(), new HashMap<>());
    }

    public MenuLayout(@NotNull List<String> layout, @NotNull Supplier<Map<Character, Icon>> layoutMapSupplier) {
        this(layout, layoutMapSupplier.get());
    }

    public MenuLayout(@NotNull Supplier<List<String>> layoutSupplier, @NotNull Supplier<Map<Character, Icon>> layoutMapSupplier) {
        this(layoutSupplier.get(), layoutMapSupplier.get());
    }

    public MenuLayout(@NotNull List<String> layout, @NotNull Map<Character, Icon> layoutMap) {
        this.layout = layout;
        this.layoutMap = layoutMap;
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

    @NotNull
    public Map<Character, Icon> layoutMap() {
        return layoutMap;
    }

    public MenuLayout setLayoutMap(@NotNull Map<Character, Icon> layoutMap) {
        this.layoutMap = layoutMap;
        return this;
    }

}
