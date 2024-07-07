package crypticlib.internal.exception;

public class UnsupportedVersionException extends RuntimeException {

    public UnsupportedVersionException(int version) {
        super(version + "");
    }

}
