package crypticlib.scheduler;

public interface TaskWrapper {

    void cancel();

    boolean isCancelled();

}
