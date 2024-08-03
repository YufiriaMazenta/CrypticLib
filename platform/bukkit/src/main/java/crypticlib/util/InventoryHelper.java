package crypticlib.util;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;

import java.lang.reflect.Method;

public class InventoryHelper {

    private static final Method inventoryEventGetViewMethod;
    private static final Method inventoryViewGetTopInventoryMethod;
    private static final Method playerGetOpenInventoryMethod;
    private static final Method inventoryViewSetTitleMethod;
    private static final Method inventoryViewGetTitleMethod;
    private static final Method inventoryViewGetPlayerMethod;

    static {
        try {
            inventoryEventGetViewMethod = ReflectionHelper.getMethod(InventoryEvent.class, "getView");
            Class<?> inventoryViewClass = Class.forName("org.bukkit.inventory.InventoryView");
            inventoryViewGetTopInventoryMethod = ReflectionHelper.getMethod(inventoryViewClass, "getTopInventory");
            playerGetOpenInventoryMethod = ReflectionHelper.getMethod(Player.class, "getOpenInventory");
            inventoryViewSetTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "setTitle", String.class);
            inventoryViewGetTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "getTitle");
            inventoryViewGetPlayerMethod = ReflectionHelper.getMethod(inventoryViewClass, "getPlayer");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static Inventory getTopInventory(HumanEntity humanEntity) {
        return getTopInventory(getOpenInventory(humanEntity));
    }

    public static Inventory getTopInventory(InventoryEvent event) {
        return getTopInventory(getInventoryView(event));
    }

    private static Inventory getTopInventory(Object inventoryView) {
        return (Inventory) ReflectionHelper.invokeMethod(inventoryViewGetTopInventoryMethod, inventoryView);
    }

    public static Object getInventoryView(InventoryEvent event) {
        return ReflectionHelper.invokeMethod(inventoryEventGetViewMethod, event);
    }

    public static void setInventoryViewTitle(Object inventoryView, String title) {
        ReflectionHelper.invokeMethod(inventoryViewSetTitleMethod, inventoryView, title);
    }

    public static String getInventoryViewTitle(Object inventoryView) {
        return (String) ReflectionHelper.invokeMethod(inventoryViewGetTitleMethod, inventoryView);
    }

    public static Object getOpenInventory(HumanEntity humanEntity) {
        return ReflectionHelper.invokeMethod(playerGetOpenInventoryMethod, humanEntity);
    }

    public static HumanEntity getInventoryViewPlayer(Object inventoryView) {
        return (HumanEntity) ReflectionHelper.invokeMethod(inventoryViewGetPlayerMethod, inventoryView);
    }

}
