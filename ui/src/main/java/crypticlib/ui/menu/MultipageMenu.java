package crypticlib.ui.menu;

import crypticlib.ui.display.Icon;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.ui.display.MenuLayout;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class MultipageMenu extends Menu {

    protected List<Icon> elements = new CopyOnWriteArrayList<>();
    protected Integer page = 0;
    protected Integer maxPage;
    protected Integer maxElementNumPerPage;
    protected Character elementKey;
    protected List<Integer> elementSlots = new ArrayList<>();

    public MultipageMenu(@NotNull Player player) {
        this(player, new MenuDisplay());
    }

    public MultipageMenu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier) {
        this(player, displaySupplier.get(), null, new ArrayList<>());
    }

    public MultipageMenu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier, Character elementKey, Supplier<List<Icon>> elementsSupplier) {
        this(player, displaySupplier.get(), elementKey, elementsSupplier.get());
    }

    public MultipageMenu(@NotNull Player player, @NotNull Supplier<MenuDisplay> displaySupplier, Character elementKey, List<Icon> elements) {
        this(player, displaySupplier.get(), elementKey, elements);
    }

    public MultipageMenu(@NotNull Player player, @NotNull MenuDisplay menuDisplay) {
        this(player, menuDisplay, null, new ArrayList<>());
    }

    public MultipageMenu(@NotNull Player player, @NotNull MenuDisplay display, Character elementKey, Supplier<List<Icon>> elementsSupplier) {
        this(player, display, elementKey, elementsSupplier.get());
    }
    
    public MultipageMenu(@NotNull Player player, @NotNull MenuDisplay display, Character elementKey, List<Icon> elements) {
        super(player, display);
        this.elementKey = elementKey;
        if (elements != null)
            this.elements.addAll(elements);
    }

    @Override
    protected void parseLayout() {
        slotMap.clear();
        layoutSlotMap.clear();
        
        //绘制除了自动生成图标以外的所有图标
        MenuLayout layout = display.layout();
        for (int x = 0; x < layout.layout().size(); x++) {
            String line = layout.layout().get(x);
            for (int y = 0; y < Math.min(line.length(), 9); y++) {
                Character key = line.charAt(y);
                int slot = x * 9 + y;
                if (key.equals(elementKey)) {
                    elementSlots.add(slot);
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
                slotMap.put(slot, layout.layoutMap().get(key));
            }
        }

        //绘制自动生成图标
        parseElements();
        //刷新页面
        refreshOpenedInventory();
    }

    protected void refreshMaxPage() {
        maxElementNumPerPage = elementSlots.size();
        if (elements.size() % maxElementNumPerPage == 0)
            maxPage = elements.size() / maxElementNumPerPage;
        else
            maxPage = elements.size() / maxElementNumPerPage + 1;
    }

    protected void parseElements() {
        refreshMaxPage();
        for (Integer slot : elementSlots) {
            slotMap.remove(slot);
        }
        int start = page * maxElementNumPerPage;
        List<Icon> pageElements = elements.subList(start, Math.min(elements.size(), start + maxElementNumPerPage));
        for (int i = 0; i < elementSlots.size() && i < pageElements.size(); i++) {
            int slot = elementSlots.get(i);
            slotMap.put(slot, pageElements.get(i));
        }
    }

    public void nextPage() {
        setPage(page + 1);
    }

    public void previousPage() {
        setPage(page - 1);
    }

    public int page() {
        return page;
    }

    public void setPage(Integer page) {
        if (page < 0 || page >= maxPage)
            return;
        this.page = page;
        parseElements();
        refreshOpenedInventory();
    }

    public int maxPage() {
        return maxPage;
    }

    public List<Icon> elements() {
        return new ArrayList<>(elements);
    }

    public MultipageMenu setElements(List<Icon> elements) {
        this.elements.clear();
        this.elements.addAll(elements);
        parseElements();
        refreshOpenedInventory();
        return this;
    }

    @Override
    public MultipageMenu openMenu() {
        return (MultipageMenu) super.openMenu();
    }

    @Override
    public MultipageMenu setDisplay(@NotNull MenuDisplay display) {
        return (MultipageMenu) super.setDisplay(display);
    }

    @Override
    public MultipageMenu setOpenAction(@Nullable BiConsumer<Menu, InventoryOpenEvent> openAction) {
        return (MultipageMenu) super.setOpenAction(openAction);
    }

    @Override
    public MultipageMenu setCloseAction(@Nullable BiConsumer<Menu, InventoryCloseEvent> closeAction) {
        return (MultipageMenu) super.setCloseAction(closeAction);
    }

}
