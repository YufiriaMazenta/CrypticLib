package crypticlib.lifecycle;

public interface LifeCycleTask<Plugin> {

    void run(Plugin plugin, LifeCycle lifeCycle);

}
