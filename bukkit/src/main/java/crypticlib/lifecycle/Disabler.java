package crypticlib.lifecycle;

/**
 * 当插件disable时，调用有注解OnDisable的接口实现类的disable方法
 * 只适用于通过此注解实例化的单例对象，通过new实例化的对象除非主动注册，否则无法自动注销
 * 主动注册：BukkitPlugin.disableableList.put
 */
public interface Disabler {

    void disable();

}
