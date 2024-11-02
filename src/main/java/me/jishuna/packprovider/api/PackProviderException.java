package me.jishuna.packprovider.api;

public class PackProviderException extends Exception {
    public PackProviderException(String message) {
        super(message);
    }

    public PackProviderException(String message, Throwable cause) {
        super(message, cause);
    }
}
