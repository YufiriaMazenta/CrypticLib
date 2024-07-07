package crypticlib.internal.exception;

public class PluginLoadException extends RuntimeException {

    public PluginLoadException(Throwable cause) {
        super("Plugin load failed.", cause);
    }

}
