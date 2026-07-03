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
import crypticlib.scheduler.Scheduler;
import crypticlib.util.ReflectionHelper;
import org.jetbrains.annotations.ApiStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public interface CrypticLibPlugin {

    @ApiStatus.Internal
    String pluginName();

    @ApiStatus.Internal
    CommandManager<?, ?> commandManager();

    @ApiStatus.Internal
    Scheduler scheduler();

    @ApiStatus.Internal
    MsgSender msgSender();

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
                    if (ignoreExceptions.contains(throwable.getClass())) {
                        return;
                    }
                    List<Class<? extends Throwable>> printExceptions = Arrays.asList(annotation.printExceptions());
                    if (printExceptions.contains(throwable.getClass())) {
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

}
