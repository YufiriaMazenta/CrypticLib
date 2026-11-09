package crypticlib.ui.menu;

import crypticlib.CrypticLibBukkit;

import java.util.Optional;
import java.util.function.Consumer;

public interface ChildMenu {

    Menu parentMenu();

    ChildMenu setParentMenu(Menu parentMenu);

    /**
     * 打开双亲页面
     * @param callback 回调函数，传入被打开的双亲页面，如果当前无双亲页面，则为空
     */
    default void openParentMenu(Consumer<Optional<Menu>> callback) {
        Menu parentMenu = parentMenu();
        if (parentMenu == null) {
            callback.accept(Optional.empty());
            return;
        }
        CrypticLibBukkit.scheduler().sync(() -> {
            parentMenu.openMenu();
            callback.accept(Optional.of(parentMenu));
        });
    }

}
