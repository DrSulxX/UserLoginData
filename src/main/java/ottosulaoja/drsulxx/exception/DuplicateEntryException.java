package ottosulaoja.drsulxx.exception;

public class DuplicateEntryException extends RuntimeException {
    private static final long serialVersionUID = 1L;  // Add this line to resolve the warning

    public DuplicateEntryException(String message) {
        super(message);
    }

    public DuplicateEntryException(String message, Throwable cause) {
        super(message, cause);
    }
}