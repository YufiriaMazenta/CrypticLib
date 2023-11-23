package crypticlib.ui;

import net.minecraft.world.inventory.Slot;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public interface Menu extends InventoryHolder {

    int menuSize();

    Map<Integer, Icon> iconMap();

    Menu setIconMap(Map<Integer, Slot> iconMap);

    default Icon setIcon(int slot, Icon icon) {
        return iconMap().put(slot, icon);
    }

    default Icon removeIcon(int slot) {
        return iconMap().remove(slot);
    }

    List<Panel> panelList();

    Menu setPanelList(List<Panel> panelList);

    default Menu addPanel(Panel panel) {
        panelList().add(panel);
        return this;
    }

    default Menu addPanel(int priority, Panel panel) {
        panelList().add(priority, panel);
        return this;
    }

    default Menu draw() {
        if (panelList() == null)
            return this;
        for (Panel panel : panelList()) {
            panel.draw(this);
        }
        return this;
    }

    default Icon click(int slot, InventoryClickEvent event) {
        if (!iconMap().containsKey(slot)) {
            event.setCancelled(true);
            return null;
        }
        return iconMap().get(slot).click(event);
    }

    @NotNull
    @Override
    default Inventory getInventory() {
        Inventory inventory = Bukkit.createInventory(this, menuSize());
        iconMap().forEach((slot, icon) -> {
            inventory.setItem(slot, icon.display());
        });
        return inventory;
    }

    void onClose(InventoryCloseEvent event);

}
