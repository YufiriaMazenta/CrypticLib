package crypticlib.resource;

public class ResourceLoadException extends RuntimeException {

    public ResourceLoadException(String message) {
        super(message);
    }

    public ResourceLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourceLoadException(Throwable cause) {
        super(cause);
    }

    public ResourceLoadException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ResourceLoadException() {
    }
}
