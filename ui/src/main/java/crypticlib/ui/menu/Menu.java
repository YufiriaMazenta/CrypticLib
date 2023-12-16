package crypticlib.ui.menu;

import crypticlib.chat.TextProcessor;
import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class Menu implements InventoryHolder {

    private final Map<Integer, Icon> slotMap;
    private final Player player;
    private MenuDisplay display;
    private BiConsumer<Menu, InventoryOpenEvent> openAction;
    private BiConsumer<Menu, InventoryCloseEvent> closeAction;
    private final Map<Character, List<Integer>> layoutSlotMap;
    private Inventory openedInventory;

    public Menu(@NotNull Player player) {
        this(player, new MenuDisplay());
    }

    public Menu(@NotNull Player player, String title) {
        this(player, new MenuDisplay(title));
    }

    public Menu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier) {
        this(player, displaySupplier.get());
    }

    public Menu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier, @Nullable BiConsumer<Menu, InventoryOpenEvent> openAction, @Nullable BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        this(player, displaySupplier.get(), openAction, closeAction);
    }

    public Menu(@NotNull Player player, @NotNull MenuDisplay display) {
        this(player, display, (m, e) -> {}, (m, e) -> {});
    }

    public Menu(@NotNull Player player, @NotNull MenuDisplay display, @Nullable BiConsumer<Menu, InventoryOpenEvent> openAction, @Nullable BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        this.player = player;
        this.display = display;
        this.slotMap = new ConcurrentHashMap<>();
        this.openAction = openAction;
        this.closeAction = closeAction;
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

    public void onOpen(InventoryOpenEvent event) {
        if (openAction != null)
            openAction.accept(this, event);
    }

    public void onClose(InventoryCloseEvent event) {
        if (closeAction != null)
            closeAction.accept(this, event);
    }


    public Menu openMenu() {
        this.openedInventory = getInventory();
        player.openInventory(openedInventory);
        return this;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        parseDisplay();
        int size;
        String title;
        if (display != null) {
            size = Math.min(display.layout().layout().size() * 9, 54);
            title = TextProcessor.color(TextProcessor.placeholder(player, display.title()));
        } else {
            size = 27;
            title = "";
        }

        Inventory inventory = Bukkit.createInventory(this, size, title);
        draw(inventory);
        return inventory;
    }

    protected Menu parseDisplay() {
        slotMap.clear();
        layoutSlotMap.clear();
        if (display == null)
            return this;
        for (int x = 0; x < display.layout().layout().size(); x++) {
            String line = display.layout().layout().get(x);
            for (int y = 0; y < Math.min(line.length(), 9); y++) {
                char key = line.charAt(y);
                if (!display.layout().layoutMap().containsKey(key)) {
                    continue;
                }
                int slot = x * 9 + y;
                if (layoutSlotMap.get(key) == null) {
                    layoutSlotMap.put(key, new ArrayList<>(Collections.singletonList(slot)));
                } else {
                    layoutSlotMap.get(key).add(slot);
                }
                slotMap.put(slot, display.layout().layoutMap().get(key));
            }
        }
        if (openedInventory != null) {
            openedInventory.clear();
            draw(openedInventory);
        }
        return this;
    }

    protected void draw(Inventory inventory) {
        slotMap.forEach((slot, icon) -> {
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

    protected @NotNull Map<Integer, Icon> slotMap() {
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
        if (openedInventory != null)
            openedInventory.setItem(slot, icon.display());
        return slotMap.put(slot, icon);
    }

    /**
     * 删除一个位置的图标
     *
     * @param slot 删除的位置
     * @return 被删除的图标
     */
    public @Nullable Icon removeIcon(int slot) {
        if (openedInventory != null)
            openedInventory.setItem(slot, new ItemStack(Material.AIR));
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
        parseDisplay();
        return this;
    }

    @Nullable
    protected Inventory openedInventory() {
        return openedInventory;
    }

    @Nullable
    public BiConsumer<Menu, InventoryOpenEvent> openAction() {
        return openAction;
    }

    public Menu setOpenAction(@Nullable BiConsumer<Menu, InventoryOpenEvent> openAction) {
        this.openAction = openAction;
        return this;
    }

    @Nullable
    public BiConsumer<Menu, InventoryCloseEvent> closeAction() {
        return closeAction;
    }

    public Menu setCloseAction(@Nullable BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        this.closeAction = closeAction;
        return this;
    }

}
