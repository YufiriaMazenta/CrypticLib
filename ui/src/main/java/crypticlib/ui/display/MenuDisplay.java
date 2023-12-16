package crypticlib.ui.display;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class MenuDisplay {

    private String title;
    private MenuLayout layout;

    public MenuDisplay() {
        this.title = "";
        this.layout = new MenuLayout();
    }

    public MenuDisplay(@NotNull Supplier<MenuLayout> layoutSupplier) {
        this(layoutSupplier.get());
    }

    public MenuDisplay(@NotNull MenuLayout layout) {
        this.layout = layout;
        this.title = "";
    }

    public MenuDisplay(@NotNull String title) {
        this.title = title;
        this.layout = new MenuLayout();
    }

    public MenuDisplay(@NotNull String title, @NotNull Supplier<MenuLayout> layoutSupplier) {
        this(title, layoutSupplier.get());
    }

    public MenuDisplay(@NotNull String title, @NotNull MenuLayout layout) {
        this.title = title;
        this.layout = layout;
    }

    public @NotNull String title() {
        return title;
    }

    public MenuDisplay setTitle(@NotNull String title) {
        this.title = title;
        return this;
    }

    @NotNull
    public MenuLayout layout() {
        return layout;
    }

    public MenuDisplay setLayout(@NotNull MenuLayout layout) {
        this.layout = layout;
        return this;
    }

}
