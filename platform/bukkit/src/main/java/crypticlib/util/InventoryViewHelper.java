package crypticlib.util;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * InventoryView兼容工具类
 * InventoryView在低版本和高版本中API有较大变化,使用此工具类统一处理
 */
public class InventoryViewHelper {

    private static final Method inventoryEventGetViewMethod;
    private static final Method playerGetOpenInventoryMethod;
    private static final Method inventoryViewCloseMethod;
    private static final Method inventoryViewConvertSlotMethod;
    private static final Method inventoryViewCountSlotsMethod;
    private static final Method inventoryViewGetBottomInventoryMethod;
    private static final Method inventoryViewGetCursorMethod;
    private static final Method inventoryViewGetInventoryMethod;
    private static final Method inventoryViewGetItemMethod;
    private static final Method inventoryViewGetOriginalTitleMethod;
    private static final Method inventoryViewGetPlayerMethod;
    private static final Method inventoryViewGetSlotTypeMethod;
    private static final Method inventoryViewGetTitleMethod;
    private static final Method inventoryViewGetTopInventoryMethod;
    private static final Method inventoryViewGetTypeMethod;
    private static final Method inventoryViewSetCursorMethod;
    private static final Method inventoryViewSetItemMethod;
    private static final Method inventoryViewSetPropertyMethod;
    private static final Method inventoryViewSetTitleMethod;

    static {
        try {
            inventoryEventGetViewMethod = ReflectionHelper.getMethod(InventoryEvent.class, "getView");
            playerGetOpenInventoryMethod = ReflectionHelper.getMethod(Player.class, "getOpenInventory");

            Class<?> inventoryViewClass = Class.forName("org.bukkit.inventory.InventoryView");
            inventoryViewCloseMethod = ReflectionHelper.getMethod(inventoryViewClass, "close");
            inventoryViewConvertSlotMethod = ReflectionHelper.getMethod(inventoryViewClass, "convertSlot", int.class);
            inventoryViewCountSlotsMethod = ReflectionHelper.getMethod(inventoryViewClass, "countSlots");
            inventoryViewGetBottomInventoryMethod = ReflectionHelper.getMethod(inventoryViewClass, "getBottomInventory");
            inventoryViewGetCursorMethod = ReflectionHelper.getMethod(inventoryViewClass, "getCursor");
            inventoryViewGetInventoryMethod = ReflectionHelper.getMethod(inventoryViewClass, "getInventory", int.class);
            inventoryViewGetItemMethod = ReflectionHelper.getMethod(inventoryViewClass, "getItem", int.class);
            //getOriginalTitle是Spigot 1.20才加入的API,旧版本缺失时置为null,不阻断整个类的初始化
            Method getOriginalTitleMethod;
            try {
                getOriginalTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "getOriginalTitle");
            } catch (NoSuchMethodException e) {
                getOriginalTitleMethod = null;
            }
            inventoryViewGetOriginalTitleMethod = getOriginalTitleMethod;
            inventoryViewGetPlayerMethod = ReflectionHelper.getMethod(inventoryViewClass, "getPlayer");
            inventoryViewGetSlotTypeMethod = ReflectionHelper.getMethod(inventoryViewClass, "getSlotType", int.class);
            inventoryViewGetTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "getTitle");
            inventoryViewGetTopInventoryMethod = ReflectionHelper.getMethod(inventoryViewClass, "getTopInventory");
            inventoryViewGetTypeMethod = ReflectionHelper.getMethod(inventoryViewClass, "getType");
            inventoryViewSetCursorMethod = ReflectionHelper.getMethod(inventoryViewClass, "setCursor", ItemStack.class);
            inventoryViewSetItemMethod = ReflectionHelper.getMethod(inventoryViewClass, "setItem", int.class, ItemStack.class);
            Class<?> inventoryViewPropertyClass = Class.forName("org.bukkit.inventory.InventoryView$Property");
            inventoryViewSetPropertyMethod = ReflectionHelper.getMethod(inventoryViewClass, "setProperty", inventoryViewPropertyClass, int.class);
            //setTitle(String)是Spigot 1.20才加入的API,旧版本缺失时置为null,不阻断整个类的初始化
            Method setTitleMethod;
            try {
                setTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "setTitle", String.class);
            } catch (NoSuchMethodException e) {
                setTitleMethod = null;
            }
            inventoryViewSetTitleMethod = setTitleMethod;
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从InventoryEvent对象上获取InventoryView对象
     * @param event 页面事件
     */
    public static Object getInventoryView(InventoryEvent event) {
        return invoke(inventoryEventGetViewMethod, event);
    }

    /**
     * 从HumanEntity上获取正在打开的InventoryView对象
     * @param humanEntity 人类实体
     */
    public static Object getOpenInventory(HumanEntity humanEntity) {
        return invoke(playerGetOpenInventoryMethod, humanEntity);
    }

    /**
     * 获取人类实体正在打开的上方页面
     * @param humanEntity 人类实体
     */
    public static Inventory getTopInventory(HumanEntity humanEntity) {
        return getTopInventory(getOpenInventory(humanEntity));
    }

    /**
     * 获取页面事件所属的InventoryView的上方页面
     * @param event 页面事件
     */
    public static Inventory getTopInventory(InventoryEvent event) {
        return getTopInventory(getInventoryView(event));
    }

    /**
     * 关闭该窗口视图
     */
    public static void close(Object inventoryView) {
        invoke(inventoryViewCloseMethod, inventoryView);
    }

    /**
     * 将原始槽位id转换为本地槽位id (本地槽位id适用于当前正在查看的两个物品栏).
     */
    public static int convertSlot(Object inventoryView, int slot) {
        return (int) invoke(inventoryViewConvertSlotMethod, inventoryView, slot);
    }

    /**
     * Check the total number of slots in this view, combining the upper and lower inventories.
     */
    public static int countSlots(Object inventoryView) {
        return (int) invoke(inventoryViewCountSlotsMethod, inventoryView);
    }

    /**
     * 获取此窗口视图下方的容器 (通常是玩家背包).
     */
    public static Inventory getBottomInventory(Object inventoryView) {
        return (Inventory) invoke(inventoryViewGetBottomInventoryMethod, inventoryView);
    }

    /**
     * 获取玩家客户端鼠标光标上的物品
     */
    public static ItemStack getCursorItem(Object inventoryView) {
        return (ItemStack) invoke(inventoryViewGetCursorMethod, inventoryView);
    }

    /**
     * 获取原始槽位所处的页面
     */
    public static Inventory getInventory(Object inventoryView, int rawSlot) {
        return (Inventory) invoke(inventoryViewGetInventoryMethod, inventoryView, rawSlot);
    }

    /**
     * 获取该物品栏指定槽位的物品.
     */
    public static ItemStack getItem(Object inventoryView, int slot) {
        return (ItemStack) invoke(inventoryViewGetItemMethod, inventoryView, slot);
    }

    /**
     * 获取原始视图标题
     */
    public static String getOriginalTitle(Object inventoryView) {
        if (inventoryViewGetOriginalTitleMethod == null)
            throw new UnsupportedOperationException("InventoryView#getOriginalTitle is not supported on this server version (requires 1.20+)");
        return (String) invoke(inventoryViewGetOriginalTitleMethod, inventoryView);
    }

    /**
     * 当前服务端版本是否支持InventoryView#getOriginalTitle (Spigot 1.20+)
     */
    public static boolean isGetOriginalTitleSupported() {
        return inventoryViewGetOriginalTitleMethod != null;
    }

    /**
     * 获取正在查看此窗口的玩家
     */
    public static HumanEntity getViewingPlayer(Object inventoryView) {
        return (HumanEntity) invoke(inventoryViewGetPlayerMethod, inventoryView);
    }

    /**
     * 获取此槽位的类型
     */
    public static InventoryType.SlotType getSlotType(Object inventoryView, int slot) {
        return (InventoryType.SlotType) invoke(inventoryViewGetSlotTypeMethod, inventoryView, slot);
    }

    /**
     * 获取此窗口的标题
     */
    public static String getTitle(Object inventoryView) {
        return (String) invoke(inventoryViewGetTitleMethod, inventoryView);
    }

    /**
     * 获取此窗口上方的容器
     */
    public static Inventory getTopInventory(Object inventoryView) {
        return (Inventory) invoke(inventoryViewGetTopInventoryMethod, inventoryView);
    }

    /**
     * 获取此窗口的类型
     */
    public static InventoryType getInventoryType(Object inventoryView) {
        return (InventoryType) invoke(inventoryViewGetTypeMethod, inventoryView);
    }

    /**
     * 设置玩家鼠标上的物品
     */
    public static void setCursor(Object inventoryView, ItemStack item) {
        invoke(inventoryViewSetCursorMethod, inventoryView, item);
    }

    /**
     * 设置指定槽位的物品
     */
    public static void setItem(Object inventoryView, int slot, ItemStack item) {
        invoke(inventoryViewSetItemMethod, inventoryView, slot, item);
    }

    /**
     * Sets an extra property of this inventory if supported by that inventory, for example the state of a progress bar.
     */
    public static void setProperty(Object inventoryView, Object property, int value) {
        invoke(inventoryViewSetPropertyMethod, inventoryView, property, value);
    }

    /**
     * 设置此窗口的标题
     */
    public static void setTitle(Object inventoryView, String title) {
        if (inventoryViewSetTitleMethod == null)
            throw new UnsupportedOperationException("InventoryView#setTitle is not supported on this server version (requires 1.20+)");
        invoke(inventoryViewSetTitleMethod, inventoryView, title);
    }

    /**
     * 当前服务端版本是否支持InventoryView#setTitle (Spigot 1.20+)
     */
    public static boolean isSetTitleSupported() {
        return inventoryViewSetTitleMethod != null;
    }

    private static Object invoke(Method method, Object obj, Object... args) {
        try {
            return ReflectionHelper.invokeMethod(method, obj, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
