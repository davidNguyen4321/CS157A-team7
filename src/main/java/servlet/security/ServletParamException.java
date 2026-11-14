package servlet.security;

public class ServletParamException extends RuntimeException {
    private final int status;

    public ServletParamException(int status, String message) {
        super(message);
        this.status = status;
    }

    public int getStatus() { return status; }
}