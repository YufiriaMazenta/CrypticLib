package crypticlib.script.func;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本函数注册中心
 * 管理所有可用的脚本函数
 */
public enum ScriptFunctionRegistry {

    INSTANCE;

    private final Map<String, ScriptFunction> functions = new ConcurrentHashMap<>();

    /**
     * 注册函数
     * @param name 函数名（脚本中调用时使用的名字）
     * @param function 函数实现
     */
    public void register(String name, ScriptFunction function) {
        functions.put(name, function);
    }

    /**
     * 注销函数
     * @param name 函数名
     */
    public void unregister(String name) {
        functions.remove(name);
    }

    /**
     * 获取函数
     * @param name 函数名
     * @return 函数实现，不存在返回 null
     */
    public ScriptFunction getFunction(String name) {
        return functions.get(name);
    }

    /**
     * 检查函数是否存在
     */
    public boolean hasFunction(String name) {
        return functions.containsKey(name);
    }

    /**
     * 获取所有已注册的函数
     */
    public Map<String, ScriptFunction> allFunctions() {
        return functions;
    }
}
