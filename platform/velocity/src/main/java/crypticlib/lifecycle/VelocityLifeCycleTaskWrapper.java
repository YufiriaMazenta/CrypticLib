package crypticlib.lifecycle;

import crypticlib.VelocityPlugin;

import java.util.List;

public class VelocityLifeCycleTaskWrapper extends LifeCycleTaskWrapper<VelocityPlugin> {

    public VelocityLifeCycleTaskWrapper(LifeCycleTask<VelocityPlugin> lifeCycleTask, int priority, List<Class<? extends Throwable>> ignoreExceptions, List<Class<? extends Throwable>> printExceptions) {
        super(lifeCycleTask, priority, ignoreExceptions, printExceptions);
    }

}
