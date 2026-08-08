package crypticlib.script.object;

import crypticlib.script.ScriptContext;
import crypticlib.script.ScriptException;
import crypticlib.script.ScriptValue;
import crypticlib.script.func.ScriptFunctionRegistry;
import crypticlib.script.func.ScriptModule;
import crypticlib.script.vm.ScriptVM;

import java.lang.reflect.Constructor;

/**
 * 对象操作脚本函数模块
 * 提供通用的 Java 对象属性访问和方法调用能力，通过方法调用语法使用:
 * <pre>
 *   player.get("name")
 *   event.get("player").get("name")
 *   event.set("cancelled", true)
 *   player.invoke("sendMessage", "Hello!")
 *   event.invoke("setCancelled", true)
 * </pre>
 * receiver 为 nil 时统一返回 nil，使链式访问可安全传播；
 * receiver 类型不对或参数个数不足时抛 {@link ScriptException}。
 */
public enum ObjectScriptModule implements ScriptModule {

    INSTANCE;

    @Override
    public String moduleName() {
        return "obj";
    }

    @Override
    public void register(ScriptFunctionRegistry registry) {
        String module = moduleName();
        registry.register(module, "get", this::getProperty);
        registry.register(module, "set", this::setProperty);
        registry.register(module, "invoke", this::invoke);
        registry.register(module, "get_class", this::getClass);
        registry.register(module, "is_subclass_of", this::isSubclassOf);
        registry.register(module, "is_superclass_of", this::isSuperclassOf);
        registry.register(module, "is_instance_of", this::isInstanceOf);
        registry.register(module, "new_instance", this::newInstance);
    }

    /**
     * obj.get("field") → 读取属性
     * args[0] = receiver (ObjectValue), args[1] = 属性名
     */
    private ScriptValue getProperty(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "get", 2, "get(name)");
        if (obj == null) return ScriptValue.nil();
        return obj.resolver().getProperty(obj.value(), args[1].asString());
    }

    /**
     * obj.set("field", value) → 设置属性
     * args[0] = receiver, args[1] = 属性名, args[2] = 值
     */
    private ScriptValue setProperty(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "set", 3, "set(name, value)");
        if (obj == null) return ScriptValue.nil();
        obj.resolver().setProperty(obj.value(), args[1].asString(), args[2]);
        return ScriptValue.nil();
    }

    /**
     * obj.invoke("methodName", arg1, arg2, ...) → 调用方法
     * args[0] = receiver (ObjectValue), args[1] = 方法名, args[2..n] = 方法参数
     */
    private ScriptValue invoke(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "invoke", 2, "invoke(method, args...)");
        if (obj == null) return ScriptValue.nil();
        String methodName = args[1].asString();
        ScriptValue[] methodArgs = new ScriptValue[args.length - 2];
        System.arraycopy(args, 2, methodArgs, 0, methodArgs.length);
        return obj.resolver().callMethod(obj.value(), methodName, methodArgs);
    }

    /**
     * obj.get_class() → 获取对象的完整类名
     * args[0] = receiver (ObjectValue)
     */
    private ScriptValue getClass(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "get_class", 1, "get_class()");
        if (obj == null) return ScriptValue.nil();
        return ScriptValue.of(obj.typeName());
    }

    /**
     * obj.is_subclass_of(other) → 判断当前对象是否是另一个对象的子类
     * args[0] = receiver (ObjectValue), args[1] = 另一个 ObjectValue
     */
    private ScriptValue isSubclassOf(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "is_subclass_of", 2, "is_subclass_of(other)");
        if (obj == null) return ScriptValue.nil();
        if (!(args[1] instanceof ScriptValue.ObjectValue)) {
            throw new ScriptException("is_subclass_of() requires an object argument");
        }
        ScriptValue.ObjectValue other = (ScriptValue.ObjectValue) args[1];
        Class<?> thisType = obj.type();
        Class<?> otherType = other.type();
        if (thisType == null || otherType == null) return ScriptValue.of(false);
        return ScriptValue.of(otherType.isAssignableFrom(thisType) && !thisType.equals(otherType));
    }

    /**
     * obj.is_superclass_of(other) → 判断当前对象是否是另一个对象的父类
     * args[0] = receiver (ObjectValue), args[1] = 另一个 ObjectValue
     */
    private ScriptValue isSuperclassOf(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "is_superclass_of", 2, "is_superclass_of(other)");
        if (obj == null) return ScriptValue.nil();
        if (!(args[1] instanceof ScriptValue.ObjectValue)) {
            throw new ScriptException("is_superclass_of() requires an object argument");
        }
        ScriptValue.ObjectValue other = (ScriptValue.ObjectValue) args[1];
        Class<?> thisType = obj.type();
        Class<?> otherType = other.type();
        if (thisType == null || otherType == null) return ScriptValue.of(false);
        return ScriptValue.of(thisType.isAssignableFrom(otherType) && !thisType.equals(otherType));
    }

    /**
     * obj.is_instance_of(className) → 判断对象是否是某个类的实例
     * args[0] = receiver (ObjectValue), args[1] = 类名（全限定名）
     */
    private ScriptValue isInstanceOf(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        ScriptValue.ObjectValue obj = receiver(args, "is_instance_of", 2, "is_instance_of(className)");
        if (obj == null) return ScriptValue.nil();
        String className = args[1].asString();
        Class<?> type = obj.type();
        if (type == null) return ScriptValue.of(false);
        try {
            Class<?> targetClass = Class.forName(className);
            return ScriptValue.of(targetClass.isAssignableFrom(type));
        } catch (ClassNotFoundException e) {
            throw new ScriptException("Class not found: " + className);
        }
    }

    /**
     * new_instance(className, arg1, arg2, ...) → 创建对象实例
     * args[0] = 类名（全限定名）, args[1..n] = 构造函数参数
     */
    private ScriptValue newInstance(ScriptContext ctx, ScriptVM vm, ScriptValue... args) {
        // 至少需要类名参数
        if (args.length < 1) {
            throw new ScriptException("new_instance(className, args...) requires at least 1 argument");
        }
        String className = args[0].asString();
        try {
            Class<?> clazz = Class.forName(className);
            // 提取构造函数参数
            ScriptValue[] constructorArgs = new ScriptValue[args.length - 1];
            System.arraycopy(args, 1, constructorArgs, 0, constructorArgs.length);
            // 查找匹配的构造函数
            Object[] convertedArgs = new Object[constructorArgs.length];
            Class<?>[] paramTypes = new Class[constructorArgs.length];
            for (int i = 0; i < constructorArgs.length; i++) {
                ScriptValue arg = constructorArgs[i];
                if (arg.isNull()) {
                    convertedArgs[i] = null;
                    paramTypes[i] = Object.class;
                } else if (arg instanceof ScriptValue.Str) {
                    convertedArgs[i] = arg.asString();
                    paramTypes[i] = String.class;
                } else if (arg instanceof ScriptValue.Int) {
                    long val = ((ScriptValue.Int) arg).value();
                    if (val >= Integer.MIN_VALUE && val <= Integer.MAX_VALUE) {
                        convertedArgs[i] = (int) val;
                        paramTypes[i] = int.class;
                    } else {
                        convertedArgs[i] = val;
                        paramTypes[i] = long.class;
                    }
                } else if (arg instanceof ScriptValue.Num) {
                    convertedArgs[i] = ((ScriptValue.Num) arg).value().doubleValue();
                    paramTypes[i] = double.class;
                } else if (arg instanceof ScriptValue.Bool) {
                    convertedArgs[i] = ((ScriptValue.Bool) arg).value();
                    paramTypes[i] = boolean.class;
                } else if (arg instanceof ScriptValue.ObjectValue) {
                    convertedArgs[i] = ((ScriptValue.ObjectValue) arg).value();
                    paramTypes[i] = ((ScriptValue.ObjectValue) arg).type() != null ? ((ScriptValue.ObjectValue) arg).type() : Object.class;
                } else {
                    convertedArgs[i] = arg.asString();
                    paramTypes[i] = String.class;
                }
            }
            // 查找匹配的构造函数
            for (Constructor<?> constructor : clazz.getDeclaredConstructors()) {
                Class<?>[] ctorParamTypes = constructor.getParameterTypes();
                if (ctorParamTypes.length != paramTypes.length) continue;
                boolean match = true;
                for (int i = 0; i < ctorParamTypes.length; i++) {
                    if (!isCompatibleType(ctorParamTypes[i], paramTypes[i])) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    constructor.setAccessible(true);
                    Object instance = constructor.newInstance(convertedArgs);
                    return new ScriptValue.ObjectValue(instance, ReflectPropertyResolver.INSTANCE);
                }
            }
            throw new ScriptException("No matching constructor found for class " + className);
        } catch (ClassNotFoundException e) {
            throw new ScriptException("Class not found: " + className);
        } catch (ScriptException e) {
            throw e;
        } catch (Exception e) {
            throw new ScriptException("Failed to create instance: " + e.getMessage());
        }
    }

    /**
     * 判断参数类型是否兼容
     */
    private boolean isCompatibleType(Class<?> formalType, Class<?> actualType) {
        if (formalType.isAssignableFrom(actualType)) return true;
        // 处理基本类型的自动装箱
        if (formalType == int.class) return actualType == int.class || actualType == Integer.class;
        if (formalType == long.class) return actualType == long.class || actualType == Long.class;
        if (formalType == double.class) return actualType == double.class || actualType == Double.class;
        if (formalType == boolean.class) return actualType == boolean.class || actualType == Boolean.class;
        if (formalType == float.class) return actualType == float.class || actualType == Float.class;
        return false;
    }

    /**
     * 校验并取出 receiver。
     * 顺序很重要：先判 nil 再判类型，否则 nil receiver 会误报类型错误，
     * 破坏 maybeNull.get("x") 的安全传播语义。
     *
     * @return receiver，若为 nil 则返回 null 表示调用方应直接返回 nil
     */
    private ScriptValue.ObjectValue receiver(ScriptValue[] args, String funcName, int minArgs, String usage) {
        if (args.length < 1) {
            // 只有当作普通函数直接调用（obj.get() 而非 x.get()）才可能到这里
            throw new ScriptException(funcName + "() requires an object receiver");
        }
        if (args[0].isNull()) {
            return null;
        }
        if (!(args[0] instanceof ScriptValue.ObjectValue)) {
            throw new ScriptException(funcName + "() requires an object receiver, got " + args[0]);
        }
        if (args.length < minArgs) {
            throw new ScriptException(usage + " requires " + (minArgs - 1) + " argument(s), got " + (args.length - 1));
        }
        return (ScriptValue.ObjectValue) args[0];
    }
}
