package crypticlib.ui.menu;

import crypticlib.CrypticLibBukkit;

public interface ChildMenu {

    Menu parentMenu();

    ChildMenu setParentMenu(Menu parentMenu);

    default void openParentMenu() {
        Menu parentMenu = parentMenu();
        if (parentMenu == null) {
            return;
        }
        CrypticLibBukkit.scheduler().sync(parentMenu::openMenu);
    }

}
