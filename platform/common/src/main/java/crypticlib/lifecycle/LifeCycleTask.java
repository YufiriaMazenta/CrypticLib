package crypticlib.lifecycle;

public interface LifeCycleTask<Plugin> {

    void lifecycle(Plugin plugin, LifeCycle lifeCycle);

}
