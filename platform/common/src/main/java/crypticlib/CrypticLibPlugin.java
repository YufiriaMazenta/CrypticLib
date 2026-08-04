package crypticlib;

import crypticlib.chat.MsgSender;
import crypticlib.command.CommandManager;
import crypticlib.internal.PluginScanner;
import crypticlib.lifecycle.Lifecycle;
import crypticlib.lifecycle.LifecycleTask;
import crypticlib.lifecycle.LifecycleTaskSettings;
import crypticlib.lifecycle.LifecycleTaskWrapper;
import crypticlib.lifecycle.LifecycleRule;
import crypticlib.perm.PermManager;
import crypticlib.scheduler.Scheduler;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * CrypticLib插件定义接口
 * 提供一些CrypticLib所必须的方法
 */
public interface CrypticLibPlugin {

    @NotNull
    String pluginName();

    @NotNull
    CommandManager<?, ?> commandManager();

    @NotNull
    Scheduler scheduler();

    @NotNull
    MsgSender msgSender();

    @NotNull
    PermManager permManager();

    @NotNull
    Invoker getConsoleInvoker();

    default void runLifecycleTasks(Lifecycle lifeCycle) {
        List<LifecycleTaskWrapper> taskWrappers = new ArrayList<>();
        PluginScanner.INSTANCE.getAnnotatedClasses(LifecycleTaskSettings.class).forEach(
            taskClass -> {
                try {
                    if (!LifecycleTask.class.isAssignableFrom(taskClass)) {
                        return;
                    }
                    LifecycleTaskSettings annotation = taskClass.getAnnotation(LifecycleTaskSettings.class);
                    if (annotation == null) {
                        return;
                    }
                    PlatformSide[] platforms = annotation.platforms();
                    if (platforms.length > 0 && !Arrays.asList(platforms).contains(CrypticLib.CURRENT_PLATFORM)) {
                        return;
                    }
                    for (LifecycleRule lifecycleRule : annotation.rules()) {
                        Lifecycle annotationLifecycle = lifecycleRule.lifeCycle();
                        int priority = lifecycleRule.priority();
                        if (annotationLifecycle.equals(lifeCycle)) {
                            LifecycleTask task = (LifecycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                            List<Class<? extends Throwable>> ignoreExceptions = Arrays.asList(annotation.ignoreExceptions());
                            List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                            LifecycleTaskWrapper wrapper = new LifecycleTaskWrapper(task, priority, ignoreExceptions, printExceptions);
                            taskWrappers.add(wrapper);
                            return;
                        }
                    }
                } catch (Throwable throwable) {
                    LifecycleTaskSettings annotation = taskClass.getAnnotation(LifecycleTaskSettings.class);
                    List<Class<? extends Throwable>> ignoreExceptions = Arrays.asList(annotation.ignoreExceptions());
                    if (isExceptionMatched(ignoreExceptions, throwable)) {
                        return;
                    }
                    List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                    if (isExceptionMatched(printExceptions, throwable)) {
                        throwable.printStackTrace();
                        return;
                    }
                    throw new RuntimeException(throwable);
                }
            }
        );
        taskWrappers.sort(Comparator.comparingInt(LifecycleTaskWrapper::priority));
        for (LifecycleTaskWrapper taskWrapper : taskWrappers) {
            taskWrapper.runLifecycleTask(this, lifeCycle);
        }
    }

    /**
     * 判断异常(含其反射包装链上的任意cause)是否匹配给定的异常类型列表。
     * <p>
     * 通过反射实例化任务时，构造器抛出的异常会被逐层包装为 InvocationTargetException、
     * RuntimeException 等，因此需要递归解包 cause 链，并使用 isAssignableFrom 以命中子类。
     *
     * @param exceptionClasses 需要匹配的异常类型
     * @param throwable        实际捕获到的异常
     * @return 异常链上是否存在匹配的异常类型
     */
    static boolean isExceptionMatched(List<Class<? extends Throwable>> exceptionClasses, Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            for (Class<? extends Throwable> exceptionClass : exceptionClasses) {
                if (exceptionClass.isAssignableFrom(current.getClass())) {
                    return true;
                }
            }
            Throwable cause = current.getCause();
            if (cause == current) {
                break;
            }
            current = cause;
        }
        return false;
    }

}
