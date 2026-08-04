package crypticlib.ui.handler;

import crypticlib.CrypticLibPlugin;
import crypticlib.PlatformSide;
import crypticlib.lifecycle.LifecycleTaskSettings;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.Lifecycle;
import crypticlib.lifecycle.LifecycleRule;
import crypticlib.listener.EventListener;
import crypticlib.ui.menu.StoredMenu;
import crypticlib.ui.util.MenuHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;

@EventListener
@LifecycleTaskSettings(rules = {@LifecycleRule(lifeCycle = Lifecycle.DISABLE)}, platforms = PlatformSide.BUKKIT)
public enum MenuHandler implements Listener, LifecycleTask {

    INSTANCE;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickMenu(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;
        MenuHelper.getMenuHolder(event).ifPresent(menu -> menu.onClick(event.getSlot(), event));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragMenu(InventoryDragEvent event) {
        MenuHelper.getMenuHolder(event).ifPresent(menu -> menu.onDrag(event));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenMenu(InventoryOpenEvent event) {
        MenuHelper.getMenuHolder(event).ifPresent(menu -> menu.onOpen(event));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseMenu(InventoryCloseEvent event) {
        MenuHelper.getMenuHolder(event).ifPresent(menu -> menu.onClose(event));
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        MenuHelper.getOpeningMenu(player).ifPresent(menu -> {
            if (menu instanceof StoredMenu) {
                ((StoredMenu) menu).refreshStoredItems(Objects.requireNonNull(menu.inventoryCache())).returnStoredItems();
            }
        });
    }

    @Override
    public void lifecycle(CrypticLibPlugin plugin, Lifecycle lifeCycle) {
        //当插件disable时,关闭所有正在使用的页面
        for (Player player : Bukkit.getOnlinePlayers()) {
            MenuHelper.getOpeningMenu(player).ifPresent(
                menu -> player.closeInventory()
            );
        }
    }

}