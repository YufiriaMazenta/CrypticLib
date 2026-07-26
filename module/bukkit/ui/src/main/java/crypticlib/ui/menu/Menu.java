package crypticlib.ui.menu;

import crypticlib.CrypticLibBukkit;
import crypticlib.DataHolder;
import crypticlib.chat.BukkitTextProcessor;
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
import java.util.function.Consumer;
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

    /**
     * 处理点击事件
     * @param slot 点击的位置
     * @param event 点击事件
     * @return 被点击的图标，如果没有则返回 Optional.empty()
     */
    public Optional<Icon> onClick(int slot, InventoryClickEvent event) {
        Inventory topInv = InventoryViewHelper.getTopInventory(event);
        if (!topInv.equals(event.getClickedInventory())) {
            event.setCancelled(true);
            return Optional.empty();
        }
        if (!slotMap.containsKey(slot)) {
            event.setCancelled(true);
            return Optional.empty();
        }
        event.setCancelled(true);
        return Optional.of(slotMap.get(slot).onClick(event));
    }

    public void onDrag(InventoryDragEvent event) {
        //普通Menu对所有点击都取消,拖拽也应保持一致:只要拖拽涉及顶部容器槽位就整体取消
        Object inventoryView = InventoryViewHelper.getInventoryView(event);
        Inventory topInv = InventoryViewHelper.getTopInventory(event);
        for (Integer rawSlot : event.getRawSlots()) {
            Inventory inventory = InventoryViewHelper.getInventory(inventoryView, rawSlot);
            if (Objects.equals(inventory, topInv)) {
                event.setCancelled(true);
                return;
            }
        }
    }

    public void onOpen(InventoryOpenEvent event) {}

    public void onClose(InventoryCloseEvent event) {}

    /**
     * 为玩家打开页面
     * @return 页面打开的结果
     */
    public MenuOpenResult openMenu() {
        if (inventoryCache == null)
            this.inventoryCache = getInventory();
        Optional<Player> playerOpt = player();
        if (!playerOpt.isPresent()) {
            return MenuOpenResult.PLAYER_OFFLINE;
        }
        playerOpt.get().openInventory(inventoryCache);
        return MenuOpenResult.SUCCESS;
    }

    /**
     * 异步调度打开页面,不阻塞调用方线程
     * 容器的渲染(涉及PlaceholderAPI解析与Bukkit容器操作)与openInventory会在玩家所属的区域线程(Folia)或主线程(Spigot)执行,
     * 以保证线程安全并兼容Folia的区域线程所有权检查
     *
     * @param callback 页面打开后要进行的操作
     */
    public void openMenuAsync(Consumer<MenuOpenResult> callback) {
        CrypticLibBukkit.scheduler().async(() -> {
            Optional<Player> playerOpt = player();
            if (!playerOpt.isPresent()) {
                callback.accept(MenuOpenResult.PLAYER_OFFLINE);
                return;
            }
            Player player = playerOpt.get();
            //渲染与打开必须在玩家所属区域线程(Folia)/主线程(Spigot)执行,不能用全局区域调度器
            CrypticLibBukkit.scheduler().runOnEntity(
                player,
                () -> {
                    try {
                        if (this.inventoryCache == null) {
                            this.inventoryCache = getInventory();
                        }
                        player.openInventory(inventoryCache);
                        callback.accept(MenuOpenResult.SUCCESS);
                    } catch (Throwable t) {
                        callback.accept(MenuOpenResult.FAILED);
                    }
                },
                () -> callback.accept(MenuOpenResult.PLAYER_OFFLINE)
            );
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
        //容器最多6行(54格),空布局兜底为1行(9格),避免0格容器在部分版本抛异常
        int rows = Math.max(Math.min(display.layout().layout().size(), 6), 1);
        int size = rows * 9;
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
        //容器最多6行,布局行数超过6时按6行截断,避免生成的slot超出容器尺寸导致draw越界
        for (int x = 0; x < Math.min(layout.layout().size(), 6); x++) {
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
        //InventoryView#setTitle是1.20才加入的API,旧版本服务器上不支持时直接跳过标题刷新
        if (!InventoryViewHelper.isSetTitleSupported()) {
            return;
        }
        Player player = player().orElse(null);
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
        if (slots.isEmpty()) {
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
    public void beforeDraw(Inventory inventory) {}

    /**
     * 绘制页面
     * @param inventory 要进行绘制的Inventory
     */
    protected void draw(Inventory inventory) {
        beforeDraw(inventory);
        slotMap.forEach((slot, icon) -> {
            if (icon == null) {
                return;
            }
            icon.setParsePlayerId(playerId);
            preprocessIconWhenDraw(slot, icon);
            ItemStack display = icon.display();
            inventory.setItem(slot, display);
        });
        onDrawCompleted(inventory);
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
    public void onDrawCompleted(Inventory inventory) {}

    /**
     * 获取此字符在页面上的所有位置
     * @param key 需要获取的字符
     * @return 返回的位置列表，如果不存在则返回空列表
     */
    @NotNull
    public List<Integer> getSlots(Character key) {
        List<Integer> slots = layoutSlotMap.get(key);
        return slots != null ? slots : Collections.emptyList();
    }

    /**
     * 获取一个位置的图标
     * @param slot 位置
     * @return 图标，如果不存在则返回 Optional.empty()
     */
    public Optional<Icon> getIcon(int slot) {
        return Optional.ofNullable(slotMap.get(slot));
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
        return BukkitTextProcessor.color(BukkitTextProcessor.placeholder(player().orElse(null), display.title()));
    }

    /**
     * 获取打开该页面的玩家,除非玩家离线,否则不会为null
     */
    public Optional<Player> player() {
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
     * 当页面还没有打开(执行{@link Menu#openMenu()}或{@link Menu#openMenuAsync(Consumer)}前)时为空
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

    public enum MenuOpenResult {

        SUCCESS,
        PLAYER_OFFLINE,
        FAILED

    }

}
