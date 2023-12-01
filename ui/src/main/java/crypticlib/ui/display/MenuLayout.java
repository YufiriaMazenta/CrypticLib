package crypticlib.ui.display;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class MenuLayout {

    private List<String> layout;
    private Map<Character, Icon> layoutMap;

    public MenuLayout(List<String> layout, Supplier<Map<Character, Icon>> layoutMapSupplier) {
        this(layout, layoutMapSupplier.get());
    }

    public MenuLayout(Supplier<List<String>> layoutSupplier, Supplier<Map<Character, Icon>> layoutMapSupplier) {
        this(layoutSupplier.get(), layoutMapSupplier.get());
    }

    public MenuLayout(List<String> layout, Map<Character, Icon> layoutMap) {
        this.layout = layout;
        this.layoutMap = layoutMap;
    }

    public List<String> layout() {
        return layout;
    }

    public MenuLayout setLayout(List<String> layout) {
        this.layout = layout;
        return this;
    }

    public Map<Character, Icon> layoutMap() {
        return layoutMap;
    }

    public MenuLayout setLayoutMap(Map<Character, Icon> layoutMap) {
        this.layoutMap = layoutMap;
        return this;
    }
}