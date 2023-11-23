package crypticlib.ui;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

//todo 设计不合理，待重构
public interface Menu extends InventoryHolder {

    List<String> shape();

    Map<Character, Icon> iconMap();

    Map<Integer, Icon> iconSlotMap();

    Menu setIconMap(Map<Integer, Icon> iconMap);

    default Icon setIcon(int slot, Icon icon) {
        return iconSlotMap().put(slot, icon);
    }

    default Icon removeIcon(int slot) {
        return iconSlotMap().remove(slot);
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
//        if (panelList() == null)
//            return this;
//        for (Panel panel : panelList()) {
//            panel.draw(this);
//        }
        //todo
        return this;
    }

    default Icon click(int slot, InventoryClickEvent event) {
        if (!iconSlotMap().containsKey(slot)) {
            event.setCancelled(true);
            return null;
        }
        return iconSlotMap().get(slot).click(event);
    }

    @NotNull
    @Override
    default Inventory getInventory() {
//        Inventory inventory = Bukkit.createInventory(this, menuSize());
//        iconSlotMap().forEach((slot, icon) -> {
//            inventory.setItem(slot, icon.display());
//        });
//        return inventory;
        return null;
    }

    /**
     * 关闭页面时执行的方法
     * @param event 关闭页面的事件
     */
    default void onClose(InventoryCloseEvent event) {}

}
