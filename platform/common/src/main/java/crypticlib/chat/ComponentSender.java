package crypticlib.chat;

import crypticlib.CommonPlayer;
import crypticlib.Invoker;
import org.jetbrains.annotations.NotNull;

/**
 * 处理聊天组件的接口
 * 提供基于平台Component类型的发送方法
 *
 * @param <Component> 平台聊天组件类型
 */
public interface ComponentSender<Component> {

    void sendComponents(Invoker receiver, @NotNull Component... components);

    void sendComponent(Invoker receiver, @NotNull Component component);

    void sendActionbarComponent(CommonPlayer player, Component component);

    void sendActionbarComponents(CommonPlayer player, Component... components);

}
