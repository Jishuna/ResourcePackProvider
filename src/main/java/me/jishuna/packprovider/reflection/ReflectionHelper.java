package me.jishuna.packprovider.reflection;

import me.jishuna.packprovider.api.ReflectionException;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

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

    public static <T> MethodAccess<T> getMethod(Class<?> target, Class<T> type, String name) {
        for (Method method : target.getDeclaredMethods()) {
            if (method.getName().equals(name) && method.getReturnType().equals(type)) {
                method.setAccessible(true);
                return new MethodAccess<>(type, method);
            }
        }

        if (target.getSuperclass() != null) {
            return getMethod(target.getSuperclass(), type, name);
        }

        throw new ReflectionException("Cannot find method with type " + type);
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
