package crypticlib.lifecycle;

/**
 * 当插件reload时，调用注解OnReload的此接口实现类的reload方法
 * 只适用于通过注解实例化的单例对象，通过new实例化的对象除非主动注册，否则无法自动注销
 * @param <Plugin> 插件实例
 */
public interface Reloader<Plugin> {

    void reload(Plugin plugin);

}
