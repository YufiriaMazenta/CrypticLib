package crypticlib.ui.display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Supplier;

public class MenuDisplay {

    private String title;
    private MenuLayout layout;

    public MenuDisplay(Supplier<MenuLayout> layoutSupplier) {
        this(layoutSupplier.get());
    }

    public MenuDisplay(MenuLayout layout) {
        this.layout = layout;
        this.title = null;
    }

    public MenuDisplay(String title) {
        this.title = title;
        this.layout = new MenuLayout(new ArrayList<>(), new HashMap<>());
    }

    public MenuDisplay(String title, Supplier<MenuLayout> layoutSupplier) {
        this(title, layoutSupplier.get());
    }

    public MenuDisplay(String title, MenuLayout layout) {
        this.title = title;
        this.layout = layout;
    }

    public String title() {
        return title;
    }

    public MenuDisplay setTitle(String title) {
        this.title = title;
        return this;
    }

    public MenuLayout layout() {
        return layout;
    }

    public MenuDisplay setLayout(MenuLayout layout) {
        this.layout = layout;
        return this;
    }

}
