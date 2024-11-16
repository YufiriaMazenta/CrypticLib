package crypticlib.lifecycle;

public enum LifeCycle {

    INIT, //插件实例初始化时
    LOAD, //插件调用onLoad方法时
    ENABLE, //插件调用onEnable方法时
    RELOAD, //插件调用reloadPlugin方法时
    ACTIVE, //服务器完全启动时(调度器开始运行)
    DISABLE, //插件调用onDisable方法时

}
