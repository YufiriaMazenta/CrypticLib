package crypticlib.ui.handler;

import crypticlib.lifecycle.AutoTask;
import crypticlib.lifecycle.BukkitLifeCycleTask;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.TaskRule;
import crypticlib.listener.EventListener;
import crypticlib.ui.menu.Menu;
import crypticlib.ui.menu.StoredMenu;
import crypticlib.ui.util.MenuHelper;
import crypticlib.util.InventoryViewHelper;
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
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

@EventListener
@AutoTask(rules = {@TaskRule(lifeCycle = LifeCycle.DISABLE)})
public enum MenuHandler implements Listener, BukkitLifeCycleTask {

    INSTANCE;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClickMenu(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;
        InventoryHolder holder = InventoryViewHelper.getTopInventory(event).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onClick(event.getSlot(), event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onDragMenu(InventoryDragEvent event) {
        InventoryHolder holder = InventoryViewHelper.getTopInventory(event).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onDrag(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onOpenMenu(InventoryOpenEvent event) {
        InventoryHolder holder = InventoryViewHelper.getTopInventory(event).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onOpen(event);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onCloseMenu(InventoryCloseEvent event) {
        InventoryHolder holder = InventoryViewHelper.getTopInventory(event).getHolder();
        if (!(holder instanceof Menu))
            return;
        ((Menu) holder).onClose(event);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        InventoryHolder topInvHolder = InventoryViewHelper.getTopInventory(player).getHolder();
        if (topInvHolder instanceof StoredMenu) {
            ((StoredMenu) topInvHolder).refreshStoredItems(event.getPlayer().getInventory()).returnStoredItems();
        }
    }

    @Override
    public void run(Plugin plugin, LifeCycle lifeCycle) {
        //当插件disable时,关闭所有正在使用的页面
        for (Player player : Bukkit.getOnlinePlayers()) {
            Menu menu = MenuHelper.getPlayerMenu(player);
            if (menu == null) {
                continue;
            }
            player.closeInventory();
        }
    }

}