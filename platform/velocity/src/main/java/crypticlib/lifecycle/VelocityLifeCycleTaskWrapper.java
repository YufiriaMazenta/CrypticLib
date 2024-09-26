package crypticlib.lifecycle;

import crypticlib.VelocityPlugin;

public class VelocityLifeCycleTaskWrapper extends LifeCycleTaskWrapper<VelocityPlugin> {

    public VelocityLifeCycleTaskWrapper(VelocityLifeCycleTask lifeCycleTask, int priority) {
        super(lifeCycleTask, priority);
    }

}
