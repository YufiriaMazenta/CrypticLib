package crypticlib.script.func;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 脚本函数注册中心
 * 管理所有可用的脚本函数，支持模块命名空间
 *
 * 函数调用方式：
 * - math.abs(-5)      // 带命名空间，明确调用
 * - abs(-5)           // 无命名空间，使用简写（无冲突时可用）
 */
public enum ScriptFunctionRegistry {

    INSTANCE;

    /** module.function → 函数实现 */
    private final Map<String, ScriptFunction> functions = new ConcurrentHashMap<>();

    /** 无命名空间的简写名 → 函数实现（仅无冲突时存在） */
    private final Map<String, ScriptFunction> shortNames = new ConcurrentHashMap<>();

    /** 已注册的简写名（用于检测冲突） */
    private final Set<String> conflictedShortNames = ConcurrentHashMap.newKeySet();

    /**
     * 注册函数（带模块名）
     * @param moduleName 模块名
     * @param functionName 函数名
     * @param function 函数实现
     */
    public void register(String moduleName, String functionName, ScriptFunction function) {
        String fullName = moduleName + "." + functionName;
        functions.put(fullName, function);

        // 处理简写名
        if (conflictedShortNames.contains(functionName)) {
            // 已冲突，不注册简写
            shortNames.remove(functionName);
        } else if (shortNames.containsKey(functionName)) {
            // 发现冲突，移除简写
            shortNames.remove(functionName);
            conflictedShortNames.add(functionName);
        } else {
            // 无冲突，注册简写
            shortNames.put(functionName, function);
        }
    }

    /**
     * 注册函数（无模块名，直接注册为全局函数）
     * @param functionName 函数名
     * @param function 函数实现
     */
    @ApiStatus.Internal
    public void register(String functionName, ScriptFunction function) {
        functions.put(functionName, function);
        if (!conflictedShortNames.contains(functionName)) {
            shortNames.put(functionName, function);
        }
    }

    /**
     * 注销模块的所有函数
     * @param moduleName 模块名
     */
    public void unregisterModule(String moduleName) {
        functions.entrySet().removeIf(entry -> entry.getKey().startsWith(moduleName + "."));
        // 重建简写名（简单实现：清空后重新注册）
        rebuildShortNames();
    }

    /**
     * 获取函数
     * @param name 函数名（支持 module.function 或 function 格式）
     * @return 函数实现，不存在返回 null
     */
    @Nullable
    public ScriptFunction getFunction(String name) {
        // 先查找完整名
        ScriptFunction func = functions.get(name);
        if (func != null) return func;

        // 再查找简写名
        return shortNames.get(name);
    }

    /**
     * 检查函数是否存在
     */
    public boolean hasFunction(String name) {
        return functions.containsKey(name) || shortNames.containsKey(name);
    }

    /**
     * 获取所有已注册的函数（包括完整名和简写名）
     */
    public Map<String, ScriptFunction> allFunctions() {
        return functions;
    }

    /**
     * 重建简写名映射
     */
    private void rebuildShortNames() {
        shortNames.clear();
        conflictedShortNames.clear();

        for (Map.Entry<String, ScriptFunction> entry : functions.entrySet()) {
            String fullName = entry.getKey();
            int dotIndex = fullName.indexOf('.');
            if (dotIndex > 0) {
                String shortName = fullName.substring(dotIndex + 1);
                if (!conflictedShortNames.contains(shortName)) {
                    if (shortNames.containsKey(shortName)) {
                        shortNames.remove(shortName);
                        conflictedShortNames.add(shortName);
                    } else {
                        shortNames.put(shortName, entry.getValue());
                    }
                }
            }
        }
    }
}
