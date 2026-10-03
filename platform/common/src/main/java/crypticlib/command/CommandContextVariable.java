package crypticlib.command;

public final class CommandContextVariable<T> {

    private final Class<T> type;
    private final T value;

    public CommandContextVariable(Class<T> type, T value) {
        this.type = type;
        this.value = value;
    }

    public Class<T> type() {
        return type;
    }

    public T value() {
        return value;
    }

}
