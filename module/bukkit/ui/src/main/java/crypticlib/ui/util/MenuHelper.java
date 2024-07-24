package crypticlib.ui.util;

import crypticlib.ui.menu.Menu;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.Nullable;

public class MenuHelper {

    public static @Nullable Menu getPlayerMenu(Player player) {
        InventoryHolder holder = player.getOpenInventory().getTopInventory().getHolder();
        if (holder == null)
            return null;
        if (!(holder instanceof Menu))
            return null;
        return (Menu) holder;
    }

}
