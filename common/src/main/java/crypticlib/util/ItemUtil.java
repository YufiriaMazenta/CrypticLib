package crypticlib.util;

import com.google.common.base.Preconditions;
import crypticlib.chat.TextProcessor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * 物品相关工具类
 */
public class ItemUtil {

    /**
     * 判断物品是否为空
     *
     * @param item 判断的物品
     * @return 返回物品是否有效，当物品为null或者空气时返回true，其余情况返回false
     */
    public static boolean isAir(ItemStack item) {
        return item == null || item.getType().equals(Material.AIR);
    }

    public static void setDisplayName(ItemStack item, String displayName) {
        setDisplayName(item, displayName, true);
    }

    public static void setDisplayName(ItemStack item, String displayName, boolean format) {
        Preconditions.checkArgument(!ItemUtil.isAir(item), "Item can not be null");
        ItemMeta meta = item.getItemMeta();
        if (format) {
            displayName = TextProcessor.color(displayName);
        }
        meta.setDisplayName(displayName);
        item.setItemMeta(meta);
    }

    public static void setLore(ItemStack item, List<String> lore) {
        setLore(item, lore, true);
    }

    public static void setLore(ItemStack item, List<String> lore, boolean format) {
        Preconditions.checkArgument(!ItemUtil.isAir(item), "Item can not be air");
        ItemMeta itemMeta = item.getItemMeta();
        if (format) {
            lore.replaceAll(TextProcessor::color);
        }
        itemMeta.setLore(lore);

        item.setItemMeta(itemMeta);
    }

}
