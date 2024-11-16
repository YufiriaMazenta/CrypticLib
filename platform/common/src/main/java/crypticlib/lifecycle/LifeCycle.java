package crypticlib.lifecycle;

public enum LifeCycle {

    LOAD, //插件调用onLoad方法时
    ENABLE, //插件调用onEnable方法时
    RELOAD, //插件调用reloadPlugin方法时
    DISABLE, //插件调用onDisable方法时

}
