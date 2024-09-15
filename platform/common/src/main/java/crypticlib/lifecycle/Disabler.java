package crypticlib.lifecycle;

/**
 * 当插件disable时，调用注解OnDisable的此接口实现类的disable方法
 * 只适用于通过注解实例化的单例对象，通过new实例化的对象除非主动注册，否则无法自动注销
 * @param <Plugin> 插件实例
 */
public interface Disabler<Plugin> {

    void onDisable(Plugin plugin);

    default int disablePriority() {
        return 0;
    }

}
