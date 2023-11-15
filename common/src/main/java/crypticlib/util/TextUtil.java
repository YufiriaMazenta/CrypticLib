package crypticlib.util;

import crypticlib.CrypticLib;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本相关的工具类
 */
@SuppressWarnings("removal")
public class TextUtil {

    private static final Pattern colorPattern = Pattern.compile("&#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");

    /**
     * 将一个文本的颜色代码进行处理
     * @param text 需要处理的文本
     * @return 处理完颜色代码的文本
     */
    public static String color(String text) {
        if (CrypticLib.minecraftVersion() >= 16) {
            StringBuilder strBuilder = new StringBuilder(text);
            Matcher matcher = colorPattern.matcher(strBuilder);
            while (matcher.find()) {
                String colorCode = matcher.group();
                String colorStr = ChatColor.of(colorCode.substring(1)).toString();
                strBuilder.replace(matcher.start(), matcher.start() + colorCode.length(), colorStr);
                matcher = colorPattern.matcher(strBuilder);
            }
            text = strBuilder.toString();
        }
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    /**
     * 将一条文本的papi变量进行处理，如果没有PlaceholderAPI插件，将会返回源文本
     * @param player papi变量的玩家
     * @param source 源文本
     * @return 处理完成的文本
     */
    public static String placeholder(Player player, String source) {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null)
            source = PlaceholderAPI.setPlaceholders(player, source);
        return source;
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @return 转化完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text) {
        BaseComponent[] baseComponents = TextComponent.fromLegacyText(text);
        return new TextComponent(baseComponents);
    }

    /**
     * 获取实体类型对应的Bungee翻译聊天组件
     * @param entityType 实体类型
     * @return 对应的Bungee翻译聊天组件
     */
    public static TranslatableComponent toTranslatableComponent(EntityType entityType) {
        return new TranslatableComponent(entityType.getTranslationKey());
    }

    /**
     * 获取实体对应的Bungee翻译聊天组件
     * @param entity 传入的实体
     * @return 对应的Bungee翻译聊天组件
     */
    public static TranslatableComponent toTranslatableComponent(Entity entity) {
        return toTranslatableComponent(entity.getType());
    }

    /**
     * 获取物品类型对应的Bungee翻译聊天组件
     * @param material 传入的物品类型
     * @return 对应的Bungee翻译聊天组件
     */
    public static TranslatableComponent toTranslatableComponent(Material material) {
        return new TranslatableComponent(material.getTranslationKey());
    }

    /**
     * 获取物品的Bungee翻译聊天组件
     * @param itemStack 传入的物品
     * @return 对应的Bungee翻译聊天组件
     */
    public static TranslatableComponent toTranslatableComponent(ItemStack itemStack) {
        return new TranslatableComponent(itemStack.getTranslationKey());
    }

}
