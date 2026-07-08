package crypticlib.script.func;

/**
 * 脚本函数模块接口
 * 用于将相关函数组织为模块，批量注册
 *
 * 注册后函数可通过两种方式调用：
 * - moduleName.functionName(args)  // 带命名空间
 * - functionName(args)             // 简写（无冲突时可用）
 */
public interface ScriptModule {

    /**
     * 模块名称（用作命名空间前缀）
     */
    String moduleName();

    /**
     * 将本模块中的所有函数注册到注册中心
     * @param registry 函数注册中心
     */
    void register(ScriptFunctionRegistry registry);

}
