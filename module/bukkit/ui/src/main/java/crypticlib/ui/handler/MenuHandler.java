package crypticlib.ui.handler;

import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.BukkitLifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
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
import org.bukkit.plugin.Plugin;

import java.util.Objects;

@EventListener
@LifeCycleTaskSettings(rules = {@TaskRule(lifeCycle = LifeCycle.DISABLE)})
public enum MenuHandler implements Listener, BukkitLifeCycleTask {

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
    public void lifecycle(Plugin plugin, LifeCycle lifeCycle) {
        //当插件disable时,关闭所有正在使用的页面
        for (Player player : Bukkit.getOnlinePlayers()) {
            MenuHelper.getOpeningMenu(player).ifPresent(
                menu -> player.closeInventory()
            );
        }
    }

}