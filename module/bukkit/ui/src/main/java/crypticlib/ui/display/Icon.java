package crypticlib.ui.display;

import crypticlib.DataHolder;
import crypticlib.chat.BukkitTextProcessor;
import crypticlib.util.ItemHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class Icon implements DataHolder {

    protected ItemStack display;
    protected Consumer<InventoryClickEvent> clickAction;
    /**
     * 用于某些情况下图标需要解析玩家变量时使用,一般为图标所属页面的玩家,默认在{@link crypticlib.ui.menu.Menu#preprocessIconWhenDraw}前赋值
     */
    private @Nullable UUID parsePlayerId;
    protected final Map<String, Object> dataMap = new ConcurrentHashMap<>();

    public Icon(@NotNull IconDisplay iconDisplay) {
        this.display = iconDisplay.display();
    }

    public Icon(@NotNull ItemStack display) {
        this(display, null);
    }

    public Icon(@NotNull ItemStack origin, @Nullable IconDisplay iconDisplay) {
        if (!ItemHelper.isAir(origin)) {
            ItemStack originClone = origin.clone();
            if (iconDisplay != null)
                this.display = iconDisplay.applyToItemStack(originClone);
            else
                this.display = originClone;
        } else {
            this.display = origin;
        }
    }

    /**
     * 执行此图标对应的操作
     *
     * @param event 传入的点击事件
     * @return 图标本身
     */
    public Icon onClick(InventoryClickEvent event) {
        if (clickAction == null) {
            event.setCancelled(true);
            return this;
        }
        clickAction.accept(event);
        return this;
    }

    /**
     * 获取图标的展示物品,返回的物品应当完成文本中变量/颜色等内容的解析
     *
     * @return 图标的展示物品
     */
    public ItemStack display() {
        ItemStack display = this.display.clone();
        ItemMeta meta = display.getItemMeta();
        if (meta != null) {
            if (meta.hasDisplayName()) {
                meta.setDisplayName(parseIconText(meta.getDisplayName()));
            }
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();
                if (lore != null) {
                    lore.replaceAll(this::parseIconText);
                }
                meta.setLore(lore);
            }
            display.setItemMeta(meta);
        }
        return display;
    }

    /**
     * 设置图标的展示物品
     *
     * @param display 设置的展示物品
     */
    public Icon setDisplay(@NotNull ItemStack display) {
        this.display = display;
        return this;
    }

    @Nullable
    public Consumer<InventoryClickEvent> clickAction() {
        return clickAction;
    }

    public Icon setClickAction(@Nullable Consumer<InventoryClickEvent> clickAction) {
        this.clickAction = clickAction;
        return this;
    }

    public Icon setName(String name) {
        ItemHelper.setDisplayName(display, name);
        return this;
    }

    public Icon setLore(List<String> lore) {
        ItemHelper.setLore(display, lore);
        return this;
    }

    public @Nullable UUID parsePlayerId() {
        return parsePlayerId;
    }

    public Icon setParsePlayerId(@Nullable UUID parsePlayerId) {
        this.parsePlayerId = parsePlayerId;
        return this;
    }

    public Optional<Player> parsePlayerOpt() {
        if (parsePlayerId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(Bukkit.getPlayer(parsePlayerId));
    }

    @Deprecated
    public @Nullable Player parsePlayer() {
        return parsePlayerOpt().orElse(null);
    }

    /**
     * 用于解析页面图标上的文本,之所以抽象为一个方法是为了方便继承者重写方法以改变图标上文本的解析逻辑
     * @return 解析后的文本
     */
    public String parseIconText(String originText) {
        Player iconParsePlayer = parsePlayer();
        return BukkitTextProcessor.color(BukkitTextProcessor.placeholder(iconParsePlayer, originText));
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
