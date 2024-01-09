package me.jishuna.resourcepackprovider;

public class ResourcePackException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ResourcePackException() {
    }

    public ResourcePackException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResourcePackException(String message) {
        super(message);
    }

    public ResourcePackException(Throwable cause) {
        super(cause);
    }
}
