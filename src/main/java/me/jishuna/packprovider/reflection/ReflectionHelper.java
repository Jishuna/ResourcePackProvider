package me.jishuna.packprovider.reflection;

import me.jishuna.packprovider.api.ReflectionException;

import java.lang.reflect.Field;

public class ReflectionHelper {

    public static <T> FieldAccess<T> getField(Class<?> target, Class<T> type, int index) {
        for (Field field : target.getDeclaredFields()) {
            if (type.isAssignableFrom(field.getType()) && index-- <= 0) {
                field.setAccessible(true);
                return new FieldAccess<>(type, field);
            }
        }

        if (target.getSuperclass() != null) {
            return getField(target.getSuperclass(), type, index);
        }

        throw new ReflectionException("Cannot find field with type " + type);
    }

    public static Class<?> getClass(String... names) {
        for (String name : names) {
            try {
                return Class.forName(name);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        throw new ReflectionException("Cannot find class with name(s) " + String.join(",", names));
    }
}
