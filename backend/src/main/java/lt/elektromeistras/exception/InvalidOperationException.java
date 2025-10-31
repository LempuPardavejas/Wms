package lt.elektromeistras.exception;

/**
 * Exception thrown when an operation is invalid in the current context.
 * For example, trying to confirm an order that is already confirmed.
 */
public class InvalidOperationException extends RuntimeException {

    public InvalidOperationException(String message) {
        super(message);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
