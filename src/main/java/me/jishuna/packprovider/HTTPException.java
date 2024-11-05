package me.jishuna.packprovider;

public class HTTPException extends RuntimeException {
    public HTTPException(String message) {
        super(message);
    }

    public HTTPException(String message, Throwable cause) {
        super(message, cause);
    }
}
