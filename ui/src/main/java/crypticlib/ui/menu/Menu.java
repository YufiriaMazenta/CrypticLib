package crypticlib.ui.menu;

import crypticlib.chat.TextProcessor;
import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.ui.display.MenuLayout;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Menu implements InventoryHolder {

    protected final Map<Integer, Icon> slotMap;
    protected final Player player;
    protected MenuDisplay display;
    protected final Map<Character, List<Integer>> layoutSlotMap;
    protected Inventory inventoryCache;

    public Menu(@NotNull Player player) {
        this(player, new MenuDisplay());
    }

    public Menu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier) {
        this(player, displaySupplier.get());
    }

    public Menu(@NotNull Player player, @NotNull MenuDisplay display) {
        this.player = player;
        this.display = display;
        this.slotMap = new ConcurrentHashMap<>();
        this.layoutSlotMap = new ConcurrentHashMap<>();
    }

    public Icon onClick(int slot, InventoryClickEvent event) {
        if (!event.getView().getTopInventory().equals(event.getClickedInventory())) {
            event.setCancelled(true);
            return null;
        }
        if (!slotMap.containsKey(slot)) {
            event.setCancelled(true);
            return null;
        }
        event.setCancelled(true);
        return slotMap.get(slot).onClick(event);
    }

    public void onDrag(InventoryDragEvent event) {}

    public void onOpen(InventoryOpenEvent event) {}

    public void onClose(InventoryCloseEvent event) {}

    /**
     * 为玩家打开页面
     * @return 页面本身
     */
    public Menu openMenu() {
        if (inventoryCache == null)
            this.inventoryCache = getInventory();
        player.openInventory(inventoryCache);
        return this;
    }

    /**
     * 获取UI
     * 渲染步骤：解析布局->生成页面容器->绘制页面图标
     * 若已经存在inventoryCache，将直接绘制页面图标并更新页面标题
     */
    @Override
    @NotNull
    public Inventory getInventory() {
        updateLayout();
        int size = Math.min(display.layout().layout().size() * 9, 54);
        Inventory inventory;
        if (inventoryCache == null) {
            String title = TextProcessor.color(TextProcessor.placeholder(player, display.title()));
            inventory = Bukkit.createInventory(this, size, title);
        } else {
            inventory = inventoryCache;
            updateMenuTitle();
        }
        draw(inventory);
        return inventory;
    }

    /**
     * 刷新布局信息，会根据MenuDisplay解析布局，但不会更新页面图标，需手动调用updateInventoryIcons方法刷新
     */
    public void updateLayout() {
        slotMap.clear();
        layoutSlotMap.clear();

        MenuLayout layout = display.layout();
        for (int x = 0; x < layout.layout().size(); x++) {
            String line = layout.layout().get(x);
            for (int y = 0; y < Math.min(line.length(), 9); y++) {
                char key = line.charAt(y);
                if (!layout.layoutMap().containsKey(key)) {
                    continue;
                }
                int slot = x * 9 + y;
                if (layoutSlotMap.get(key) == null) {
                    layoutSlotMap.put(key, new ArrayList<>(Collections.singletonList(slot)));
                } else {
                    layoutSlotMap.get(key).add(slot);
                }
                slotMap.put(slot, layout.layoutMap().get(key));
            }
        }
    }

    /**
     * 刷新页面图标，此方法不会重新解析布局
     * 若需要重新解析布局，请调用updateLayout方法
     */
    public void updateMenuIcons() {
        if (inventoryCache != null) {
            inventoryCache.clear();
            draw(inventoryCache);
        }
    }

    /**
     * 刷新页面标题，若玩家未打开此页面，则无效
     */
    public void updateMenuTitle() {
        InventoryView inventoryView = player.getOpenInventory();
        Inventory topInventory = inventoryView.getTopInventory();
        if (topInventory.getHolder() != null && topInventory.getHolder() instanceof Menu) {
            inventoryView.setTitle(TextProcessor.color(TextProcessor.placeholder(player, display.title())));
        }
    }

    /**
     * 刷新页面，此方法不会重新解析布局
     */
    public void updateMenu() {
        updateMenu(false);
    }

    /**
     * 刷新页面
     * @param updateLayout 是否更新布局
     */
    public void updateMenu(boolean updateLayout) {
        if (updateLayout)
            updateLayout();
        updateMenuIcons();
        updateMenuTitle();
    }

    protected void draw(Inventory inventory) {
        slotMap.forEach((slot, icon) -> {
            if (icon == null) {
                return;
            }
            ItemStack display = icon.display().clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(TextProcessor.color(TextProcessor.placeholder(player, meta.getDisplayName())));
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.replaceAll(source -> TextProcessor.color(TextProcessor.placeholder(player, source)));
                }
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inventory.setItem(slot, display);
        });
    }

    public @NotNull Map<Integer, Icon> slotMap() {
        return slotMap;
    }

    /**
     * 获取此字符在页面上的所有位置
     * @param key 需要获取的字符
     * @return 返回的位置列表
     */
    public @Nullable List<Integer> getSlots(Character key) {
        return layoutSlotMap.get(key);
    }

    /**
     * 获取一个位置的图标
     * @param slot 位置
     * @return 图标
     */
    public @Nullable Icon getIcon(int slot) {
        return slotMap.get(slot);
    }

    /**
     * 设置一个位置的图标
     *
     * @param slot 设置的位置
     * @param icon 设置的图标
     * @return 如果覆盖了某图标将返回被覆盖的图标
     */
    public @Nullable Icon setIcon(int slot, Icon icon) {
        if (inventoryCache != null)
            inventoryCache.setItem(slot, icon.display());
        return slotMap.put(slot, icon);
    }

    /**
     * 删除一个位置的图标
     *
     * @param slot 删除的位置
     * @return 被删除的图标
     */
    public @Nullable Icon removeIcon(int slot) {
        if (inventoryCache != null)
            inventoryCache.setItem(slot, new ItemStack(Material.AIR));
        return slotMap.remove(slot);
    }

    @NotNull
    public Player player() {
        return player;
    }

    @NotNull
    public MenuDisplay display() {
        return display;
    }

    public Menu setDisplay(@NotNull MenuDisplay display) {
        this.display = display;
        updateLayout();
        return this;
    }

    @Nullable
    public Inventory inventoryCache() {
        return inventoryCache;
    }

}
