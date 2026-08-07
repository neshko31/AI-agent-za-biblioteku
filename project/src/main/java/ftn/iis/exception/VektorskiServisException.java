package ftn.iis.exception;

public class VektorskiServisException extends RuntimeException {
    public VektorskiServisException(String message) {
        super(message);
    }

    public VektorskiServisException(String message, Throwable cause) {
        super(message, cause);
    }
}
