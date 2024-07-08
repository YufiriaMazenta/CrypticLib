package crypticlib.internal.exception;

public class PluginEnableException extends RuntimeException {

    public PluginEnableException(Throwable cause) {
        super("Plugin enable failed.", cause);
    }

}
