package crypticlib.script.event;

import crypticlib.script.ScriptValue;

/**
 * 属性解析器 — 定义如何从 Java 对象读写属性
 * 由各平台模块实现（通用反射 resolver 或平台特化 resolver）
 */
public interface PropertyResolver {

    /**
     * 读取属性
     * @param target 目标对象
     * @param propertyName 属性名
     * @return 属性值（包装为 ScriptValue），属性不存在返回 nil
     */
    ScriptValue getProperty(Object target, String propertyName);

    /**
     * 设置属性
     * @param target 目标对象
     * @param propertyName 属性名
     * @param value 新值
     */
    void setProperty(Object target, String propertyName, ScriptValue value);

    /**
     * 调用方法
     * @param target 目标对象
     * @param methodName 方法名
     * @param args 参数
     * @return 返回值（包装为 ScriptValue），void 方法返回 nil
     */
    ScriptValue callMethod(Object target, String methodName, ScriptValue... args);

    /**
     * 包装 Java 对象为 ScriptValue（保持 resolver 引用以支持链式访问）
     */
    default ScriptValue wrap(Object javaObject) {
        return ScriptValue.of(javaObject, this);
    }
}
