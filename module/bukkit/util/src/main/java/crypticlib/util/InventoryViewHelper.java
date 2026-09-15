package crypticlib.util;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.lang.invoke.MethodHandle;

/**
 * InventoryView兼容工具类
 * InventoryView在1.20.6开始由抽象类修改为接口,导致存在不兼容问题,使用此工具类统一处理
 */
public class InventoryViewHelper {

    private static final MethodHandle inventoryEventGetViewMethod;
    private static final MethodHandle playerGetOpenInventoryMethod;
    private static final MethodHandle inventoryViewCloseMethod;
    private static final MethodHandle inventoryViewConvertSlotMethod;
    private static final MethodHandle inventoryViewCountSlotsMethod;
    private static final MethodHandle inventoryViewGetBottomInventoryMethod;
    private static final MethodHandle inventoryViewGetCursorMethod;
    private static final MethodHandle inventoryViewGetInventoryMethod;
    private static final MethodHandle inventoryViewGetItemMethod;
    private static final MethodHandle inventoryViewGetOriginalTitleMethod;
    private static final MethodHandle inventoryViewGetPlayerMethod;
    private static final MethodHandle inventoryViewGetSlotTypeMethod;
    private static final MethodHandle inventoryViewGetTitleMethod;
    private static final MethodHandle inventoryViewGetTopInventoryMethod;
    private static final MethodHandle inventoryViewGetTypeMethod;
    private static final MethodHandle inventoryViewSetCursorMethod;
    private static final MethodHandle inventoryViewSetItemMethod;
    private static final MethodHandle inventoryViewSetPropertyMethod;
    private static final MethodHandle inventoryViewSetTitleMethod;

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
            MethodHandle getOriginalTitleMethod;
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
            MethodHandle setTitleMethod;
            try {
                setTitleMethod = ReflectionHelper.getMethod(inventoryViewClass, "setTitle", String.class);
            } catch (NoSuchMethodException e) {
                setTitleMethod = null;
            }
            inventoryViewSetTitleMethod = setTitleMethod;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从InventoryEvent对象上获取InventoryView对象
     * @param event 页面事件
     */
    public static Object getInventoryView(InventoryEvent event) {
        try {
            return inventoryEventGetViewMethod.invoke(event);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 从HumanEntity上获取正在打开的InventoryView对象
     * @param humanEntity 人类实体
     */
    public static Object getOpenInventory(HumanEntity humanEntity) {
        try {
            return playerGetOpenInventoryMethod.invoke(humanEntity);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
        try {
            inventoryViewCloseMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 将原始槽位id转换为本地槽位id (本地槽位id适用于当前正在查看的两个物品栏).
     */
    public static int convertSlot(Object inventoryView, int slot) {
        try {
            return (int) inventoryViewConvertSlotMethod.invoke(inventoryView, slot);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 检查此界面中的总槽位数，包括上层容器和下层容器
     */
    public static int countSlots(Object inventoryView) {
        try {
            return (int) inventoryViewCountSlotsMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取此窗口视图下方的容器 (通常是玩家背包).
     */
    public static Inventory getBottomInventory(Object inventoryView) {
        try {
            return (Inventory) inventoryViewGetBottomInventoryMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取玩家客户端鼠标光标上的物品
     */
    public static ItemStack getCursorItem(Object inventoryView) {
        try {
            return (ItemStack) inventoryViewGetCursorMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取原始槽位所处的页面
     */
    public static Inventory getInventory(Object inventoryView, int rawSlot) {
        try {
            return (Inventory) inventoryViewGetInventoryMethod.invoke(inventoryView, rawSlot);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取该物品栏指定槽位的物品.
     */
    public static ItemStack getItem(Object inventoryView, int slot) {
        try {
            return (ItemStack) inventoryViewGetItemMethod.invoke(inventoryView, slot);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取原始视图标题
     */
    public static String getOriginalTitle(Object inventoryView) {
        if (inventoryViewGetOriginalTitleMethod == null)
            throw new UnsupportedOperationException("InventoryView#getOriginalTitle is not supported on this server version (requires 1.20+)");
        try {
            return (String) inventoryViewGetOriginalTitleMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
        try {
            return (HumanEntity) inventoryViewGetPlayerMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取此槽位的类型
     */
    public static InventoryType.SlotType getSlotType(Object inventoryView, int slot) {
        try {
            return (InventoryType.SlotType) inventoryViewGetSlotTypeMethod.invoke(inventoryView, slot);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取此窗口的标题
     */
    public static String getTitle(Object inventoryView) {
        try {
            return (String) inventoryViewGetTitleMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取此窗口上方的容器
     */
    public static Inventory getTopInventory(Object inventoryView) {
        try {
            return (Inventory) inventoryViewGetTopInventoryMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 获取此窗口的类型
     */
    public static InventoryType getInventoryType(Object inventoryView) {
        try {
            return (InventoryType) inventoryViewGetTypeMethod.invoke(inventoryView);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置玩家鼠标上的物品
     */
    public static void setCursor(Object inventoryView, ItemStack item) {
        try {
            inventoryViewSetCursorMethod.invoke(inventoryView, item);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置指定槽位的物品
     */
    public static void setItem(Object inventoryView, int slot, ItemStack item) {
        try {
            inventoryViewSetItemMethod.invoke(inventoryView, slot, item);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 如果该容器支持，则设置此容器的额外属性，例如进度条的状态
     */
    public static void setProperty(Object inventoryView, Object property, int value) {
        try {
            inventoryViewSetPropertyMethod.invoke(inventoryView, property, value);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 设置此窗口的标题
     */
    public static void setTitle(Object inventoryView, String title) {
        if (inventoryViewSetTitleMethod == null)
            throw new UnsupportedOperationException("InventoryView#setTitle is not supported on this server version (requires 1.20+)");
        try {
            inventoryViewSetTitleMethod.invoke(inventoryView, title);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 当前服务端版本是否支持InventoryView#setTitle (Spigot 1.20+)
     */
    public static boolean isSetTitleSupported() {
        return inventoryViewSetTitleMethod != null;
    }

}
