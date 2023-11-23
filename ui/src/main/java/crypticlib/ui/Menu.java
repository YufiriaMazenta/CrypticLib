package crypticlib.ui;

import crypticlib.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Menu implements InventoryHolder {

    private List<String> layout;
    private Map<Character, Icon> layoutIconMap;
    private Map<Integer, Icon> slotMap;
    private String title;

    public Menu(List<String> layout, Map<Character, Icon> layoutIconMap, String title) {
        this.layout = layout;
        this.layoutIconMap = layoutIconMap;
        this.title = title;
        this.slotMap = new ConcurrentHashMap<>();
        parseLayout();
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

    public Menu onClose(InventoryCloseEvent event) {
        return this;
    }

    public Inventory build(Player player) {
        int size = Math.min(layout.size() * 9, 54);
        title = TextUtil.color(TextUtil.placeholder(player, title));
        Inventory inventory = Bukkit.createInventory(this, size, title);
        slotMap.forEach((slot, icon) -> {
            ItemStack display = icon.display().clone();
            ItemMeta meta = display.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(TextUtil.color(TextUtil.placeholder(player, meta.getDisplayName())));
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.replaceAll(source -> TextUtil.color(TextUtil.placeholder(player, source)));
                }
                meta.setLore(lore);
                display.setItemMeta(meta);
            }
            inventory.setItem(slot, display);
        });
        return inventory;
    }

    public Menu parseLayout() {
        slotMap.clear();
        for (int x = 0; x < layout.size(); x++) {
            String line = layout.get(x);
            for (int y = 0; y < Math.min(line.length(), 9); y++) {
                char signal = line.charAt(y);
                if (!layoutIconMap.containsKey(signal)) {
                    continue;
                }
                int slot = x * 9 + y;
                slotMap.put(slot, layoutMap().get(signal));
            }
        }
        return this;
    }

    public List<String> layout() {
        return new ArrayList<>(layout);
    }

    public Menu setLayout(List<String> layout) {
        if (layout.size() > 6) {
            throw new IllegalArgumentException("Layouts with more than six rows are not supported");
        }
        this.layout = layout;
        parseLayout();
        return this;
    }

    public Map<Character, Icon> layoutMap() {
        return new ConcurrentHashMap<>(layoutIconMap);
    }

    public Menu setLayoutMap(Map<Character, Icon> layoutMap) {
        this.layoutIconMap = layoutMap;
        return this;
    }

    public Map<Integer, Icon> slotMap() {
        return slotMap;
    }

    public Menu setSlotMap(Map<Integer, Icon> slotMap) {
        this.slotMap = slotMap;
        return this;
    }

    /**
     * 设置一个位置的图标
     * @param slot 设置的位置
     * @param icon 设置的图标
     * @return 如果覆盖了某图标将返回被覆盖的图标
     */
    public Icon setIcon(int slot, Icon icon) {
        return slotMap.put(slot, icon);
    }

    /**
     * 删除一个位置的图标
     * @param slot 删除的位置
     * @return 被删除的图标
     */
    public Icon removeIcon(int slot) {
        return slotMap.remove(slot);
    }

    public String title() {
        return title;
    }

    public Menu setTitle(String title) {
        this.title = title;
        return this;
    }

    @NotNull
    @Override
    public Inventory getInventory() {
        throw new UnsupportedOperationException("To create a menu, you must specify a player to resolve the placeholders for the menu and icon");
    }

}
