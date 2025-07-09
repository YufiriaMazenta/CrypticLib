package crypticlib.ui.menu;

import crypticlib.CrypticLibBukkit;
import crypticlib.DataHolder;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.scheduler.task.TaskWrapper;
import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.ui.display.MenuLayout;
import crypticlib.ui.util.MenuHelper;
import crypticlib.util.InventoryViewHelper;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class Menu implements InventoryHolder, DataHolder {

    protected final Map<Integer, Icon> slotMap;
    protected final UUID playerId;
    protected MenuDisplay display;
    protected final Map<Character, List<Integer>> layoutSlotMap;
    protected @Nullable Inventory inventoryCache;
    protected final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    public Menu(@NotNull Player player) {
        this(player, new MenuDisplay());
    }

    public Menu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier) {
        this(player, displaySupplier.get());
    }

    public Menu(@NotNull Player player, @NotNull MenuDisplay display) {
        this.playerId = player.getUniqueId();
        this.display = display;
        this.slotMap = new LinkedHashMap<>();
        this.layoutSlotMap = new LinkedHashMap<>();
    }

    public Icon onClick(int slot, InventoryClickEvent event) {
        Inventory topInv = InventoryViewHelper.getTopInventory(event);
        if (!topInv.equals(event.getClickedInventory())) {
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
        playerOpt().ifPresent(player -> {
            player.openInventory(inventoryCache);
        });
        return this;
    }

    /**
     * 异步渲染页面,然后为玩家打开页面
     *
     * @return 执行异步操作的任务
     */
    public TaskWrapper openMenuAsync() {
        return CrypticLibBukkit.scheduler().async(() -> {
            if (this.inventoryCache == null) {
                this.inventoryCache = getInventory();
            }
            CrypticLibBukkit.scheduler().sync(() -> {
                playerOpt().ifPresent(player -> {
                    player.openInventory(inventoryCache);
                });
            });
        });
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
            inventory = Bukkit.createInventory(this, size, parsedMenuTitle());
        } else {
            inventory = inventoryCache;
            updateMenuTitle();
        }
        draw(inventory);
        return inventory;
    }

    /**
     * 在布局刷新前会调用此方法
     */
    public void beforeUpdateLayout() {}

    /**
     * 刷新布局信息，会根据MenuDisplay解析布局，但不会更新页面图标，需手动调用updateInventoryIcons方法刷新
     */
    public void updateLayout() {
        slotMap.clear();
        layoutSlotMap.clear();
        beforeUpdateLayout();

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
                Icon icon = layout.layoutMap().get(key).get();
                preprocessIconWhenUpdateLayout(slot, icon);
                slotMap.put(slot, icon);
            }
        }
        onLayoutUpdated();
    }

    /**
     * 当页面更新布局时,对Icon进行预处理
     * @param slot 此Icon所处的slot
     * @param icon 预处理的Icon
     */
    public void preprocessIconWhenUpdateLayout(Integer slot, @NotNull Icon icon) {}

    /**
     * 当页面布局更新完毕时调用
     */
    public void onLayoutUpdated() {}

    /**
     * 刷新页面图标，此方法不会重新解析布局
     * 若需要重新解析布局，请调用updateLayout方法
     */
    public void updateMenuIcons() {
        if (inventoryCache != null) {
            //只把有按钮的槽位清空
            slotMap.forEach((slot, icon) -> {
                inventoryCache.setItem(slot, new ItemStack(Material.AIR));
            });
            draw(inventoryCache);
        }
    }

    /**
     * 刷新页面标题，若玩家未打开此页面，则无效
     */
    public void updateMenuTitle() {
        Player player = player();
        if (player == null) {
            return;
        }
        MenuHelper.getOpeningMenu(player).ifPresent(
            menu -> {
                Object inventoryView = InventoryViewHelper.getOpenInventory(player);
                InventoryViewHelper.setTitle(inventoryView, parsedMenuTitle());
            }
        );
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

    /**
     * 更新一个字符对应的所有图标
     * 只有当页面已经打开才有效
     */
    public void updateIcons(char iconKey) {
        if (inventoryCache == null)
            return;
        List<Integer> slots = getSlots(iconKey);
        if (slots == null || slots.isEmpty()) {
            return;
        }
        for (Integer slot : slots) {
            Icon icon = slotMap.get(slot);
            if (icon != null) {
                inventoryCache.setItem(slot, icon.display());
            }
        }
    }

    /**
     * 更新一个图标
     * 只有当页面已经打开才有效
     * @param slot 更新的图标位置
     */
    public void updateIcon(int slot) {
        if (inventoryCache == null)
            return;
        Icon icon = slotMap.get(slot);
        if (icon != null) {
            inventoryCache.setItem(slot, icon.display());
        }
    }

    /**
     * 当页面开始绘制前会调用此方法
     */
    public void beforeDraw() {}

    /**
     * 绘制页面
     * @param inventory 要进行绘制的Inventory
     */
    protected void draw(Inventory inventory) {
        beforeDraw();
        slotMap.forEach((slot, icon) -> {
            if (icon == null) {
                return;
            }
            icon.setParsePlayerId(playerId);
            preprocessIconWhenDraw(slot, icon);
            ItemStack display = icon.display();
            inventory.setItem(slot, display);
        });
        onDrawCompleted();
    }

    /**
     * 绘制页面时,对Icon进行预处理
     * @param slot icon所处的slot
     * @param icon 要处理的icon
     */
    public void preprocessIconWhenDraw(Integer slot, @NotNull Icon icon) {}

    /**
     * 当页面图标完成绘制时调用此方法
     */
    public void onDrawCompleted() {}

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

    /**
     * 获取解析完毕后的标题,UI的最终标题将会使用此方法的结果
     * 可以继承重写以改变页面上显示的标题
     * @return 解析完成的标题
     */
    public String parsedMenuTitle() {
        return BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player(), display.title()));
    }

    /**
     * 获取打开此页面的玩家,除非玩家离线,否则不会为null
     */
    @Deprecated
    @Nullable
    public Player player() {
        return playerOpt().orElse(null);
    }

    /**
     * 获取打开该页面的玩家,除非玩家离线,否则不会为null
     */
    public Optional<@Nullable Player> playerOpt() {
        return Optional.ofNullable(Bukkit.getPlayer(playerId));
    }

    /**
     * 获取此页面的图标映射map
     */
    public @NotNull Map<Integer, Icon> slotMap() {
        return slotMap;
    }

    /**
     * 获取打开此页面的玩家的UUID
     */
    public UUID playerId() {
        return playerId;
    }

    /**
     * 获取此页面的展示内容
     */
    @NotNull
    public MenuDisplay display() {
        return display;
    }

    /**
     * 设置此页面的展示内容
     */
    public Menu setDisplay(@NotNull MenuDisplay display) {
        this.display = display;
        updateLayout();
        return this;
    }

    /**
     * 获取此页面的容器缓存
     * 当页面还没有打开(执行{@link Menu#openMenu()}或{@link Menu#openMenuAsync()}前)时为空
     */
    @Nullable
    public Inventory inventoryCache() {
        return inventoryCache;
    }

    @Override
    public Map<String, Object> allData() {
        return dataMap;
    }

    @Override
    public void setAllData(Map<String, Object> data) {
        this.dataMap.clear();
        this.dataMap.putAll(data);
    }

    @Override
    public Optional<Object> getData(String key) {
        if (dataMap.containsKey(key)) {
            return Optional.ofNullable(dataMap.get(key));
        }
        return Optional.empty();
    }

    @Override
    public Object putData(String key, Object value) {
        return dataMap.put(key, value);
    }

    @Override
    public void clearData() {
        dataMap.clear();
    }

}
