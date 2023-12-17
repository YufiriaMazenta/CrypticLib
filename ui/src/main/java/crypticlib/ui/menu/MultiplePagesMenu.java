package crypticlib.ui.menu;

import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.ui.display.MenuLayout;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MultiplePagesMenu extends Menu {

    protected List<Icon> autoIcons = new CopyOnWriteArrayList<>();
    protected int page = 0;
    protected int maxPage;
    protected char autoSlotsKey;
    protected List<Integer> autoSlots = new CopyOnWriteArrayList<>();

    public MultiplePagesMenu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier, char autoSlotsKey, List<Icon> autoIcons) {
        this(player, displaySupplier.get(), autoSlotsKey, autoIcons);
    }

    public MultiplePagesMenu(@NotNull Player player, @NotNull MenuDisplay display, char autoSlotsKey, List<Icon> autoIcons) {
        super(player, display);
        this.autoSlotsKey = autoSlotsKey;
        this.autoIcons.addAll(autoIcons);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return super.getInventory();
    }

    @Override
    protected MultiplePagesMenu parseLayout() {
        slotMap.clear();
        layoutSlotMap.clear();

        if (display == null)
            return this;
        //绘制除了自动生成图标以外的所有图标
        MenuLayout layout = display.layout();
        for (int x = 0; x < layout.layout().size(); x++) {
            String line = layout.layout().get(x);
            for (int y = 0; y < Math.min(line.length(), 9); y++) {
                char key = line.charAt(y);
                int slot = x * 9 + y;
                if (key == autoSlotsKey) {
                    autoSlots.add(slot);
                    continue;
                }
                if (!layout.layoutMap().containsKey(key)) {
                    continue;
                }
                if (layoutSlotMap.get(key) == null) {
                    layoutSlotMap.put(key, new ArrayList<>(Collections.singletonList(slot)));
                } else {
                    layoutSlotMap.get(key).add(slot);
                }
                slotMap.put(slot, display.layout().layoutMap().get(key));
            }
        }
        //绘制自动生成图标
        int maxAutoSlots = autoSlots.size();
        if (autoIcons.size() % maxAutoSlots == 0)
            maxPage = autoIcons.size() / maxAutoSlots;
        else
            maxPage = autoIcons.size() / maxAutoSlots + 1;


        if (openedInventory != null) {
            openedInventory.clear();
            draw(openedInventory);
        }
        return this;
    }

    @Override
    protected void draw(Inventory inventory) {
        super.draw(inventory);
    }

    public MultiplePagesMenu nextPage() {
        return setPage(page + 1);
    }

    public MultiplePagesMenu previousPage() {
        return setPage(page - 1);
    }

    public int page() {
        return page;
    }

    protected MultiplePagesMenu setPage(int page) {
        if (page < 0 || page > maxPage)
            return this;
        this.page = page;
        parseLayout();
        return this;
    }

    public int maxPage() {
        return maxPage;
    }

    public MultiplePagesMenu setMaxPage(int maxPage) {
        this.maxPage = maxPage;
        return this;
    }

    public List<Icon> icons() {
        return autoIcons;
    }

    @Override
    public MultiplePagesMenu openMenu() {
        return (MultiplePagesMenu) super.openMenu();
    }

    @Override
    public MultiplePagesMenu setDisplay(@NotNull MenuDisplay display) {
        return (MultiplePagesMenu) super.setDisplay(display);
    }

    @Override
    public MultiplePagesMenu setOpenAction(@Nullable BiConsumer<Menu, InventoryOpenEvent> openAction) {
        return (MultiplePagesMenu) super.setOpenAction(openAction);
    }

    @Override
    public MultiplePagesMenu setCloseAction(@Nullable BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        return (MultiplePagesMenu) super.setCloseAction(closeAction);
    }

}
