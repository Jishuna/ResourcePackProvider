package me.jishuna.packprovider.reflection;

public class ReflectionException extends RuntimeException {

    public ReflectionException(String message) {
        super(message);
    }

    public ReflectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
