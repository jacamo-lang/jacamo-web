package jacamo.web.exception;

public class SystemOverloadException extends Exception {
    private static final long serialVersionUID = 1L;

    public SystemOverloadException(String message) {
        super(message);
    }
}
