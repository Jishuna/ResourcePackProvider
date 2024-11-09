package me.jishuna.packprovider.reflection;

import java.lang.reflect.Field;

public class FieldAccess<T> {

    private final Class<T> clazz;
    private final Field field;

    public FieldAccess(Class<T> clazz, Field field) {
        this.clazz = clazz;
        this.field = field;
    }

    public T read(Object instance) throws ReflectionException {
        try {
            return this.clazz.cast(field.get(instance));
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new ReflectionException("Failed to read field value", e);
        }
    }

    public T readSafe(Object instance, T def) {
        try {
            return this.clazz.cast(field.get(instance));
        } catch (ReflectiveOperationException | ClassCastException e) {
            return def;
        }
    }

    public void write(Object instance, T value) throws ReflectionException {
        try {
            field.set(instance, value);
        } catch (ReflectiveOperationException e) {
            throw new ReflectionException("Failed to write field value", e);
        }
    }

    public boolean writeSafe(Object instance, T value) {
        try {
            field.set(instance, value);
            return true;
        } catch (ReflectiveOperationException e) {
            return false;
        }
    }
}
