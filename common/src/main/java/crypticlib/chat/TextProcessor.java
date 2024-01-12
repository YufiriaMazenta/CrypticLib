package crypticlib.chat;

import crypticlib.CrypticLib;
import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文本相关的工具类
 */
@SuppressWarnings("removal")
public class TextProcessor {

    private static final Pattern colorPattern = Pattern.compile("&#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})");

    /**
     * 将一个文本的颜色代码进行处理
     *
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
     *
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
     *
     * @param text 原始文本
     * @return 转化完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text) {
        return toComponent(text, new ArrayList<>());
    }

    /**
     * 将文本转化为Bungee聊天组件
     *
     * @param text 原始文本
     * @param clickEvent 文本的点击事件
     * @return 转化完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, ClickEvent clickEvent) {
        return toComponent(text, (HoverEvent) null, clickEvent);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param hoverEvent 文本的悬停展示事件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, HoverEvent hoverEvent) {
        return toComponent(text, hoverEvent, null);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param hoverEvent 文本的悬停展示事件
     * @param clickEvent 文本的点击事件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, HoverEvent hoverEvent, ClickEvent clickEvent) {
        return toComponent(text, new ArrayList<>(), hoverEvent, clickEvent);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param extra 附加聊天组件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, BaseComponent extra) {
        return toComponent(text, extra, null, null);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param extra 附加聊天组件
     * @param clickEvent 文本的点击事件，如果附加组件已有点击事件，则附加组件会保留自己的点击事件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, BaseComponent extra, ClickEvent clickEvent) {
        return toComponent(text, extra, null, clickEvent);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param extra 附加聊天组件
     * @param hoverEvent 文本的悬停展示事件，如果附加组件已有悬停展示事件，则附加组件会保留自己的悬停展示事件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, BaseComponent extra, HoverEvent hoverEvent) {
        return toComponent(text, extra, hoverEvent, null);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param extra 附加聊天组件
     * @param clickEvent 文本的点击事件，如果附加组件已有点击事件，则附加组件会保留自己的点击事件
     * @param hoverEvent 文本的悬停展示事件，如果附加组件已有悬停展示事件，则附加组件会保留自己的悬停展示事件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, BaseComponent extra, HoverEvent hoverEvent, ClickEvent clickEvent) {
        return toComponent(text, Collections.singletonList(extra), hoverEvent, clickEvent);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param extra 附加聊天组件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, List<BaseComponent> extra) {
        return toComponent(text, extra, null, null);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param extra 附加聊天组件
     * @param clickEvent 文本的点击事件，如果附加组件已有点击事件，则附加组件会保留自己的点击事件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, List<BaseComponent> extra, ClickEvent clickEvent) {
        return toComponent(text, extra, null, clickEvent);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param extra 附加聊天组件
     * @param hoverEvent 文本的悬停展示事件，如果附加组件已有悬停展示事件，则附加组件会保留自己的悬停展示事件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, List<BaseComponent> extra, HoverEvent hoverEvent) {
        return toComponent(text, extra, hoverEvent, null);
    }

    /**
     * 将文本转化为Bungee聊天组件
     * @param text 原始文本
     * @param extra 附加聊天组件
     * @param clickEvent 文本的点击事件，如果附加组件已有点击事件，则附加组件会保留自己的点击事件
     * @param hoverEvent 文本的悬停展示事件，如果附加组件已有悬停展示事件，则附加组件会保留自己的悬停展示事件
     * @return 转换完毕的Bungee聊天组件
     */
    public static BaseComponent toComponent(String text, List<BaseComponent> extra, HoverEvent hoverEvent, ClickEvent clickEvent) {
        BaseComponent[] baseComponents = TextComponent.fromLegacyText(text);
        TextComponent textComponent = new TextComponent(baseComponents);
        textComponent.setHoverEvent(hoverEvent);
        textComponent.setClickEvent(clickEvent);
        if (extra.isEmpty())
            return textComponent;
        for (BaseComponent component : extra) {
            textComponent.addExtra(component);
        }
        return textComponent;
    }

    /**
     * 获取实体类型对应的Bungee翻译聊天组件
     *
     * @param entityType 实体类型
     * @return 对应的Bungee翻译聊天组件
     */
    public static TranslatableComponent toTranslatableComponent(@NotNull EntityType entityType) {
        return new TranslatableComponent(entityType.getTranslationKey());
    }

    /**
     * 获取实体对应的Bungee翻译聊天组件
     *
     * @param entity 传入的实体
     * @return 对应的Bungee翻译聊天组件
     */
    public static TranslatableComponent toTranslatableComponent(@NotNull Entity entity) {
        return toTranslatableComponent(entity.getType());
    }

    /**
     * 获取物品类型对应的Bungee翻译聊天组件
     *
     * @param material 传入的物品类型
     * @return 对应的Bungee翻译聊天组件
     */
    public static TranslatableComponent toTranslatableComponent(@NotNull Material material) {
        return new TranslatableComponent(material.getTranslationKey());
    }

    /**
     * 获取物品的Bungee翻译聊天组件
     *
     * @param itemStack 传入的物品
     * @return 对应的Bungee翻译聊天组件
     */
    public static TranslatableComponent toTranslatableComponent(@NotNull ItemStack itemStack) {
        return new TranslatableComponent(itemStack.getTranslationKey());
    }

    public static HoverEvent hoverText(String text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(text));
    }

    public static HoverEvent hoverText(BaseComponent textComponent) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new BaseComponent[]{textComponent});
    }

    public static HoverEvent hoverText(Text text) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, text);
    }

    public static HoverEvent hoverText(Text... texts) {
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, texts);
    }

    public static ClickEvent clickOpenUrl(String url) {
        return new ClickEvent(ClickEvent.Action.OPEN_URL, url);
    }

    public static ClickEvent clickOpenFile(String fileUrl) {
        return new ClickEvent(ClickEvent.Action.OPEN_FILE, fileUrl);
    }

    public static ClickEvent clickRunCmd(String command) {
        return new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
    }

    public static ClickEvent clickSuggestCmd(String command) {
        return new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command);
    }

    public static ClickEvent clickChangePage(String page) {
        return new ClickEvent(ClickEvent.Action.CHANGE_PAGE, page);
    }

    public static ClickEvent clickCopyToClipboard(String text) {
        return new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, text);
    }

    public static BaseComponent setInsertion(BaseComponent baseComponent, String insertion) {
        baseComponent.setInsertion(insertion);
        return baseComponent;
    }

    public static BaseComponent setClickEvent(BaseComponent baseComponent, ClickEvent clickEvent) {
        baseComponent.setClickEvent(clickEvent);
        return baseComponent;
    }

    public static BaseComponent setHoverEvent(BaseComponent baseComponent, HoverEvent hoverEvent) {
        baseComponent.setHoverEvent(hoverEvent);
        return baseComponent;
    }

}
