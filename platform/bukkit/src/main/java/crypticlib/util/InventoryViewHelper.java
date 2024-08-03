package crypticlib.util;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Method;

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
            inventoryViewGetOriginalTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "getOriginalTitle");
            inventoryViewGetPlayerMethod = ReflectionHelper.getMethod(inventoryViewClass, "getPlayer");
            inventoryViewGetSlotTypeMethod = ReflectionHelper.getMethod(inventoryViewClass, "getSlotType", int.class);
            inventoryViewGetTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "getTitle");
            inventoryViewGetTopInventoryMethod = ReflectionHelper.getMethod(inventoryViewClass, "getTopInventory");
            inventoryViewGetTypeMethod = ReflectionHelper.getMethod(inventoryViewClass, "getType");
            inventoryViewSetCursorMethod = ReflectionHelper.getMethod(inventoryViewClass, "setCursor", ItemStack.class);
            inventoryViewSetItemMethod = ReflectionHelper.getMethod(inventoryViewClass, "setItem", int.class, ItemStack.class);
            Class<?> inventoryViewPropertyClass = Class.forName("org.bukkit.inventory.InventoryView$Property");
            inventoryViewSetPropertyMethod = ReflectionHelper.getMethod(inventoryViewClass, "setProperty", inventoryViewPropertyClass, int.class);
            inventoryViewSetTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "setTitle", String.class);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从InventoryEvent对象上获取InventoryView对象
     * @param event 页面事件
     */
    public static Object getInventoryView(InventoryEvent event) {
        return ReflectionHelper.invokeMethod(inventoryEventGetViewMethod, event);
    }

    /**
     * 从HumanEntity上获取正在打开的InventoryView对象
     * @param humanEntity 人类实体
     */
    public static Object getOpenInventory(HumanEntity humanEntity) {
        return ReflectionHelper.invokeMethod(playerGetOpenInventoryMethod, humanEntity);
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
        ReflectionHelper.invokeMethod(inventoryViewCloseMethod, inventoryView);
    }

    /**
     * 将原始槽位id转换为本地槽位id (本地槽位id适用于当前正在查看的两个物品栏).
     */
    public static int convertSlot(Object inventoryView, int slot) {
        return (int) ReflectionHelper.invokeMethod(inventoryViewConvertSlotMethod, inventoryView, slot);
    }

    /**
     * Check the total number of slots in this view, combining the upper and lower inventories.
     */
    public static int countSlots(Object inventoryView) {
        return (int) ReflectionHelper.invokeMethod(inventoryViewCountSlotsMethod, inventoryView);
    }

    /**
     * 获取此窗口视图下方的容器 (通常是玩家背包).
     */
    public static Inventory getBottomInventory(Object inventoryView) {
        return (Inventory) ReflectionHelper.invokeMethod(inventoryViewGetBottomInventoryMethod, inventoryView);
    }

    /**
     * 获取玩家客户端鼠标光标上的物品
     */
    public static ItemStack getCursorItem(Object inventoryView) {
        return (ItemStack) ReflectionHelper.invokeMethod(inventoryViewGetCursorMethod, inventoryView);
    }

    /**
     * 获取原始槽位所处的页面
     */
    public static Inventory getInventory(Object inventoryView, int rawSlot) {
        return (Inventory) ReflectionHelper.invokeMethod(inventoryViewGetInventoryMethod, inventoryView, rawSlot);
    }

    /**
     * 获取该物品栏指定槽位的物品.
     */
    public static ItemStack getItem(Object inventoryView, int slot) {
        return (ItemStack) ReflectionHelper.invokeMethod(inventoryViewGetItemMethod, inventoryView, slot);
    }

    /**
     * 获取原始视图标题
     */
    public static String getOriginalTitle(Object inventoryView) {
        return (String) ReflectionHelper.invokeMethod(inventoryViewGetOriginalTitleMethod, inventoryView);
    }

    /**
     * 获取正在查看此窗口的玩家
     */
    public static HumanEntity getPlayer(Object inventoryView) {
        return (HumanEntity) ReflectionHelper.invokeMethod(inventoryViewGetPlayerMethod, inventoryView);
    }

    /**
     * 获取此槽位的类型
     */
    public static InventoryType.SlotType getSlotType(Object inventoryView, int slot) {
        return (InventoryType.SlotType) ReflectionHelper.invokeMethod(inventoryViewGetSlotTypeMethod, inventoryView, slot);
    }

    /**
     * 获取此窗口的标题
     */
    public static String getTitle(Object inventoryView) {
        return (String) ReflectionHelper.invokeMethod(inventoryViewGetTitleMethod, inventoryView);
    }

    /**
     * 获取此窗口上方的容器
     */
    public static Inventory getTopInventory(Object inventoryView) {
        return (Inventory) ReflectionHelper.invokeMethod(inventoryViewGetTopInventoryMethod, inventoryView);
    }

    /**
     * 获取此窗口的类型
     */
    public static InventoryType getType(Object inventoryView) {
        return (InventoryType) ReflectionHelper.invokeMethod(inventoryViewGetTypeMethod, inventoryView);
    }

    /**
     * 设置玩家鼠标上的物品
     */
    public static void setCursor(Object inventoryView, ItemStack item) {
        ReflectionHelper.invokeMethod(inventoryViewSetCursorMethod, inventoryView, item);
    }

    /**
     * 设置指定槽位的物品
     */
    public static void setItem(Object inventoryView, int slot, ItemStack item) {
        ReflectionHelper.invokeMethod(inventoryViewSetItemMethod, inventoryView, slot, item);
    }

    /**
     * Sets an extra property of this inventory if supported by that inventory, for example the state of a progress bar.
     */
    public static void setProperty(Object inventoryView, Object property, int value) {
        ReflectionHelper.invokeMethod(inventoryViewSetPropertyMethod, inventoryView, property, value);
    }

    /**
     * 设置此窗口的标题
     */
    public static void setTitle(Object inventoryView, String title) {
        ReflectionHelper.invokeMethod(inventoryViewSetTitleMethod, inventoryView, title);
    }


}
