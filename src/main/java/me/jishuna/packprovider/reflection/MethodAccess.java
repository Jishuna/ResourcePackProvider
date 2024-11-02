package me.jishuna.packprovider.reflection;

import me.jishuna.packprovider.api.ReflectionException;

import java.lang.reflect.Method;

public class MethodAccess<T> {

    private final Class<T> clazz;
    private final Method method;

    public MethodAccess(Class<T> clazz, Method method) {
        this.clazz = clazz;
        this.method = method;
    }

    public T invoke(Object instance, Object... args) throws ReflectionException {
        try {
            return clazz.cast(method.invoke(instance, args));
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new ReflectionException("Failed to invoke method", e);
        }
    }

    public T invokeSafe(Object instance, T def, Object... args) {
        try {
            return clazz.cast(method.invoke(instance, args));
        } catch (ReflectiveOperationException | ClassCastException e) {
            return def;
        }
    }
}
