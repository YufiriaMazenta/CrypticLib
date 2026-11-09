package crypticlib.ui.menu;

import crypticlib.ui.display.Icon;
import crypticlib.ui.display.IconCreator;
import crypticlib.ui.display.MenuDisplay;
import crypticlib.ui.display.MenuLayout;
import crypticlib.util.ItemHelper;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.InvocationHandlerAdapter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

import static net.bytebuddy.matcher.ElementMatchers.isFinalizer;
import static net.bytebuddy.matcher.ElementMatchers.isVirtual;
import static net.bytebuddy.matcher.ElementMatchers.not;
import static org.junit.jupiter.api.Assertions.*;

/** Uses real menu logic with in-memory inventories; no Minecraft server is required. */
class MenuLayoutRefreshTest {

    @Test
    void removedIconsAreNotReturnedWhenMenuCloses() {
        Fixture fixture = new Fixture();
        fixture.layout.setLayout(Collections.singletonList("         "));

        fixture.menu.updateMenu(true);
        fixture.close();

        fixture.assertOnlyPlayerItemsReturned();
        assertTrue(ItemHelper.isAir(fixture.top.items[0]));
        assertTrue(ItemHelper.isAir(fixture.top.items[4]));
    }

    @Test
    void movingAnIconClearsItsOldSlotButPreservesOtherSlots() {
        Fixture fixture = new Fixture();
        fixture.layout.setLayout(Collections.singletonList(" A  B    "));

        fixture.menu.updateMenu(true);

        assertTrue(ItemHelper.isAir(fixture.top.items[0]));
        assertEquals(Material.DIAMOND, fixture.top.items[1].getType());
        assertEquals(Material.EMERALD, fixture.top.items[4].getType());
        assertSame(fixture.playerItems, fixture.top.items[8]);
        fixture.close();
        fixture.assertOnlyPlayerItemsReturned();
    }

    @Test
    void layoutOnlyUpdateRemovesRetiredIconsBeforeCloseWithoutRedraw() {
        Fixture fixture = new Fixture();
        fixture.layout.setLayout(Collections.singletonList("    B    "));

        fixture.menu.updateLayout();
        fixture.close();

        fixture.assertOnlyPlayerItemsReturned();
        assertTrue(ItemHelper.isAir(fixture.top.items[0]));
        assertEquals(Material.EMERALD, fixture.top.items[4].getType());
    }

    @Test
    void replacingTheDisplayRemovesRetiredIconsBeforeCloseWithoutRedraw() {
        Fixture fixture = new Fixture();

        fixture.menu.setDisplay(new MenuDisplay(layout("         ")));
        fixture.close();

        fixture.assertOnlyPlayerItemsReturned();
        assertTrue(ItemHelper.isAir(fixture.top.items[0]));
        assertTrue(ItemHelper.isAir(fixture.top.items[4]));
    }

    @Test
    void cachedInventoryRerenderAlsoRemovesRetiredIcons() {
        Fixture fixture = new Fixture();
        fixture.layout.setLayout(Collections.singletonList("    B    "));

        assertSame(fixture.top.api, fixture.menu.getInventory());
        assertTrue(ItemHelper.isAir(fixture.top.items[0]));
        assertSame(fixture.playerItems, fixture.top.items[8]);
        fixture.close();
        fixture.assertOnlyPlayerItemsReturned();
    }

    @Test
    void anotherLayoutUpdateDoesNotClearPlayerItemsPlacedInAFreedSlot() {
        Fixture fixture = new Fixture();
        fixture.layout.setLayout(Collections.singletonList("    B    "));
        fixture.menu.updateLayout();
        assertTrue(ItemHelper.isAir(fixture.top.items[0]));
        ItemStack newlyStored = new ItemStack(Material.GOLD_INGOT, 2);
        fixture.top.api.setItem(0, newlyStored);

        fixture.menu.updateLayout();

        assertSame(newlyStored, fixture.top.items[0]);
        assertSame(fixture.playerItems, fixture.top.items[8]);
        fixture.close();
        assertEquals(2, fixture.backpack.received.size());
        assertEquals(2, fixture.backpack.returnedAmount(Material.GOLD_INGOT));
        assertEquals(3, fixture.backpack.returnedAmount(Material.IRON_INGOT));
        assertEquals(0, fixture.backpack.returnedAmount(Material.DIAMOND));
        assertEquals(0, fixture.backpack.returnedAmount(Material.EMERALD));
    }

    @Test
    void unchangedLayoutLeavesExistingIconsAndPlayerItemsInPlace() {
        Fixture fixture = new Fixture();
        ItemStack diamond = fixture.top.items[0];
        ItemStack emerald = fixture.top.items[4];

        fixture.menu.updateLayout();

        assertSame(diamond, fixture.top.items[0]);
        assertSame(emerald, fixture.top.items[4]);
        assertSame(fixture.playerItems, fixture.top.items[8]);
        fixture.close();
        fixture.assertOnlyPlayerItemsReturned();
    }

    @Test
    void repeatedRefreshAndCloseDoesNotDuplicateReturnedItems() {
        Fixture fixture = new Fixture();
        fixture.layout.setLayout(Collections.singletonList(" A  B    "));
        fixture.menu.updateMenu(true);
        fixture.menu.updateMenu(true);

        fixture.close();
        fixture.close();

        fixture.assertOnlyPlayerItemsReturned();
        assertTrue(fixture.menu.storedItems().isEmpty());
        assertTrue(ItemHelper.isAir(fixture.top.items[0]));
    }

    @Test
    void layoutsCanBeUpdatedBeforeAnInventoryHasBeenCreated() {
        Fixture fixture = new Fixture();
        TestStoredMenu unopened = new TestStoredMenu(fixture.player, new MenuDisplay(layout("A        ")));

        unopened.updateLayout();
        assertTrue(unopened.slotMap().containsKey(0));
        unopened.setDisplay(new MenuDisplay(layout("         ")));

        assertTrue(unopened.slotMap().isEmpty());
        assertNull(unopened.inventoryCache());
    }

    @Test
    void layoutHooksKeepTheirOrderAndOldIconsAreGoneBeforeTheCompletionHook() {
        Fixture fixture = new Fixture();
        fixture.menu.hooks.clear();
        fixture.layout.setLayout(Collections.singletonList("    B    "));
        fixture.menu.onLayoutCompleted = () -> {
            assertTrue(ItemHelper.isAir(fixture.top.items[0]));
            assertSame(fixture.playerItems, fixture.top.items[8]);
        };

        fixture.menu.updateLayout();

        assertEquals(Arrays.asList("before", "after"), fixture.menu.hooks);
    }

    private static MenuLayout layout(String row) {
        Map<Character, IconCreator> icons = new HashMap<>();
        icons.put('A', () -> new RawIcon(Material.DIAMOND));
        icons.put('B', () -> new RawIcon(Material.EMERALD));
        return new MenuLayout(Collections.singletonList(row), icons);
    }

    private static final class Fixture {
        private final InventoryStub top = new InventoryStub(9, Inventory.class);
        private final InventoryStub backpack = new InventoryStub(36, PlayerInventory.class);
        private final ItemStack playerItems = new ItemStack(Material.IRON_INGOT, 3);
        private final Player player;
        private final InventoryView view;
        private final MenuLayout layout = layout("A   B    ");
        private final TestStoredMenu menu;

        private Fixture() {
            UUID playerId = UUID.randomUUID();
            player = proxy(Player.class, (object, method, args) -> {
                switch (method.getName()) {
                    case "getUniqueId": return playerId;
                    case "getInventory": return backpack.api;
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
            view = proxy(InventoryView.class, (object, method, args) -> {
                switch (method.getName()) {
                    case "getTopInventory": return top.api;
                    case "getBottomInventory": return backpack.api;
                    case "getPlayer": return player;
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
            menu = new TestStoredMenu(player, new MenuDisplay(layout));
            menu.inventoryCache = top.api;
            menu.updateMenu(true);
            top.api.setItem(8, playerItems);
        }

        private void close() {
            menu.onClose(new InventoryCloseEvent(view));
        }

        private void assertOnlyPlayerItemsReturned() {
            assertEquals(1, backpack.received.size());
            assertEquals(3, backpack.returnedAmount(Material.IRON_INGOT));
            assertEquals(0, backpack.returnedAmount(Material.DIAMOND));
            assertEquals(0, backpack.returnedAmount(Material.EMERALD));
        }
    }

    private static final class TestStoredMenu extends StoredMenu {
        private final Player testPlayer;
        private final List<String> hooks = new ArrayList<>();
        private Runnable onLayoutCompleted = () -> {};

        private TestStoredMenu(Player player, MenuDisplay display) {
            super(player, display);
            testPlayer = player;
        }

        @Override
        public Optional<Player> player() {
            return Optional.of(testPlayer);
        }

        @Override
        public void updateMenuTitle() {
            // Title rendering is unrelated to inventory reconciliation and requires a live server.
        }

        @Override
        public void beforeUpdateLayout() {
            hooks.add("before");
        }

        @Override
        public void onLayoutUpdated() {
            hooks.add("after");
            onLayoutCompleted.run();
        }
    }

    private static final class RawIcon extends Icon {
        private RawIcon(Material material) {
            super(new ItemStack(material));
        }

        @Override
        public ItemStack display() {
            // Avoid the live-server ItemFactory used for lore/placeholder rendering.
            return display.clone();
        }
    }

    private static final class InventoryStub {
        private final ItemStack[] items;
        private final List<ItemStack> received = new ArrayList<>();
        private final Inventory api;

        private InventoryStub(int size, Class<? extends Inventory> type) {
            items = new ItemStack[size];
            api = proxy(type, (object, method, args) -> {
                switch (method.getName()) {
                    case "getSize": return items.length;
                    case "getItem": return items[(Integer) args[0]];
                    case "setItem": items[(Integer) args[0]] = (ItemStack) args[1]; return null;
                    case "addItem":
                        for (ItemStack item : (ItemStack[]) args[0]) {
                            if (!ItemHelper.isAir(item)) {
                                received.add(item.clone());
                            }
                        }
                        return new HashMap<Integer, ItemStack>();
                    default: throw new UnsupportedOperationException(method.getName());
                }
            });
        }

        private int returnedAmount(Material material) {
            return received.stream().filter(item -> item.getType() == material)
                .mapToInt(ItemStack::getAmount).sum();
        }
    }

    private static <T> T proxy(Class<T> type, InvocationHandler handler) {
        // JDK 17's Proxy eagerly initializes API parameter types and requires Bukkit registries.
        // These inventory tests only need boundary calls, not a server or registry contents.
        InvocationHandler wrapper = (object, method, args) -> {
            switch (method.getName()) {
                case "hashCode": return System.identityHashCode(object);
                case "equals": return object == args[0];
                case "toString": return "Test" + type.getSimpleName();
                default: return handler.invoke(object, method, args);
            }
        };
        try {
            return type.cast(new ByteBuddy()
                .subclass(Object.class)
                .implement(type)
                .method(isVirtual().and(not(isFinalizer())))
                .intercept(InvocationHandlerAdapter.of(wrapper))
                .make()
                .load(type.getClassLoader(), ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance());
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError("Cannot create test " + type.getSimpleName(), exception);
        }
    }
}
