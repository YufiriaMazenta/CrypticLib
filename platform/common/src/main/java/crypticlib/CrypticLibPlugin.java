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

import java.util.*;

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

    Optional<CommonPlayer> getCrypticLibPlayer(UUID uuid);

    Optional<CommonPlayer> getCrypticLibPlayer(String playerName);

    default void runLifecycleTasks(Lifecycle lifeCycle) {
        List<LifecycleTaskWrapper> syncTaskWrappers = new ArrayList<>();
        List<LifecycleTaskWrapper> asyncTaskWrappers = new ArrayList<>();
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
                        boolean async = lifecycleRule.isAsync();
                        if (annotationLifecycle.equals(lifeCycle)) {
                            LifecycleTask task = (LifecycleTask) ReflectionHelper.getSingletonClassInstance(taskClass);
                            List<Class<? extends Throwable>> ignoreExceptions = Arrays.asList(annotation.ignoreExceptions());
                            List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                            LifecycleTaskWrapper wrapper = new LifecycleTaskWrapper(task, priority, ignoreExceptions, printExceptions);
                            if (async) {
                                asyncTaskWrappers.add(wrapper);
                            } else {
                                syncTaskWrappers.add(wrapper);
                            }
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

        if (!syncTaskWrappers.isEmpty()) {
            syncTaskWrappers.sort(Comparator.comparingInt(LifecycleTaskWrapper::priority));
            for (LifecycleTaskWrapper syncTaskWrapper : syncTaskWrappers) {
                CrypticLib.debug("Call lifecycle task | Lifecycle: " + lifeCycle.name() + ", Class: " + syncTaskWrapper.lifeCycleTask().getClass().getName());
                syncTaskWrapper.runLifecycleTask(this, lifeCycle);
            }
        }
        if (asyncTaskWrappers.isEmpty()) {
            return;
        }
        asyncTaskWrappers.sort(Comparator.comparing(LifecycleTaskWrapper::priority));
        Runnable asyncTasksRunnable = () -> {
            for (LifecycleTaskWrapper asyncTaskWrapper : asyncTaskWrappers) {
                CrypticLib.debug("Call async lifecycle task | Lifecycle: " + lifeCycle.name() + ", Class: " + asyncTaskWrapper.lifeCycleTask().getClass().getName());
                asyncTaskWrapper.runLifecycleTask(this, lifeCycle);
            }
        };
        switch (lifeCycle) {
            case INIT:
            case LOAD:
            case ENABLE:
                //init, load和enable阶段无法使用bukkit等平台的调度器,只能自己临时new一个线程来执行
                new Thread(asyncTasksRunnable, "CrypticLibAsyncLifecycleTask-" + lifeCycle.name()).start();
                break;
            case ACTIVE:
            case RELOAD:
                CrypticLib.plugin().scheduler().async(asyncTasksRunnable);
                break;
            case DISABLE:
                //这个阶段大部分情况下是服务器关闭,这时候异步线程无法保证一定正确执行,所以同步执行
                asyncTasksRunnable.run();
                break;
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
