package crypticlib.internal;

import crypticlib.CrypticLib;
import crypticlib.PlatformSide;
import crypticlib.chat.MsgSender;
import crypticlib.command.CommandManager;
import crypticlib.lifecycle.LifeCycle;
import crypticlib.lifecycle.LifeCycleTask;
import crypticlib.lifecycle.LifeCycleTaskSettings;
import crypticlib.lifecycle.LifeCycleTaskWrapper;
import crypticlib.lifecycle.TaskRule;
import crypticlib.perm.PermManager;
import crypticlib.scheduler.Scheduler;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@ApiStatus.Internal
public interface CrypticLibPlugin {

    String pluginName();

    CommandManager<?, ?> commandManager();

    Scheduler scheduler();

    MsgSender msgSender();

    PermManager permManager();

    default void runLifeCycleTasks(Object plugin, LifeCycle lifeCycle) {
        List<LifeCycleTaskWrapper> taskWrappers = new ArrayList<>();
        PluginScanner.INSTANCE.getAnnotatedClasses(LifeCycleTaskSettings.class).forEach(
            taskClass -> {
                try {
                    if (!LifeCycleTask.class.isAssignableFrom(taskClass)) {
                        return;
                    }
                    LifeCycleTaskSettings annotation = taskClass.getAnnotation(LifeCycleTaskSettings.class);
                    if (annotation == null) {
                        return;
                    }
                    PlatformSide[] platforms = annotation.platforms();
                    if (platforms.length > 0 && !Arrays.asList(platforms).contains(CrypticLib.CURRENT_PLATFORM)) {
                        return;
                    }
                    for (TaskRule taskRule : annotation.rules()) {
                        LifeCycle annotationLifeCycle = taskRule.lifeCycle();
                        int priority = taskRule.priority();
                        if (annotationLifeCycle.equals(lifeCycle)) {
                            LifeCycleTask task = (LifeCycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                            List<Class<? extends Throwable>> ignoreExceptions = Arrays.asList(annotation.ignoreExceptions());
                            List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                            LifeCycleTaskWrapper wrapper = new LifeCycleTaskWrapper(task, priority, ignoreExceptions, printExceptions);
                            taskWrappers.add(wrapper);
                            return;
                        }
                    }
                } catch (Throwable throwable) {
                    LifeCycleTaskSettings annotation = taskClass.getAnnotation(LifeCycleTaskSettings.class);
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
        taskWrappers.sort(Comparator.comparingInt(LifeCycleTaskWrapper::priority));
        for (LifeCycleTaskWrapper taskWrapper : taskWrappers) {
            taskWrapper.runLifecycleTask(plugin, lifeCycle);
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
