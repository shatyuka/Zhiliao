package com.shatyuka.zhiliao.xposed;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public final class XposedHelpers {
    private XposedHelpers() {}

    public static Class<?> findClass(String name, ClassLoader loader) {
        try { return Class.forName(name, false, loader); }
        catch (ClassNotFoundException e) { throw new IllegalStateException(e); }
    }

    public static XC_MethodHook.Unhook findAndHookMethod(Class<?> type, String name, Object... args) {
        XC_MethodHook callback = callback(args);
        Class<?>[] parameters = parameterTypes(args);
        Method method = findMethodExact(type, name, parameters);
        return XposedBridge.hookMethod(method, callback);
    }

    public static XC_MethodHook.Unhook findAndHookConstructor(Class<?> type, Object... args) {
        XC_MethodHook callback = callback(args);
        Class<?>[] parameters = parameterTypes(args);
        try {
            Constructor<?> constructor = type.getDeclaredConstructor(parameters);
            return XposedBridge.hookMethod(constructor, callback);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }

    public static Object callMethod(Object receiver, String name, Object... args) {
        try {
            Method method = findCompatibleMethod(receiver.getClass(), name, args, false);
            method.setAccessible(true);
            return method.invoke(receiver, args);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }

    public static Object callStaticMethod(Class<?> type, String name, Object... args) {
        try {
            Method method = findCompatibleMethod(type, name, args, true);
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (ReflectiveOperationException e) { throw new IllegalStateException(e); }
    }

    private static XC_MethodHook callback(Object[] args) {
        if (args.length == 0 || !(args[args.length - 1] instanceof XC_MethodHook))
            throw new IllegalArgumentException("Last argument must be XC_MethodHook");
        return (XC_MethodHook) args[args.length - 1];
    }

    private static Class<?>[] parameterTypes(Object[] args) {
        Class<?>[] result = new Class<?>[args.length - 1];
        for (int i = 0; i < result.length; i++) {
            if (!(args[i] instanceof Class<?>)) throw new IllegalArgumentException("Parameter must be Class");
            result[i] = (Class<?>) args[i];
        }
        return result;
    }

    private static Method findMethodExact(Class<?> type, String name, Class<?>[] parameters) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try { return current.getDeclaredMethod(name, parameters); }
            catch (NoSuchMethodException ignored) {}
        }
        throw new IllegalStateException(new NoSuchMethodException(type.getName() + '#' + name));
    }

    private static Method findCompatibleMethod(Class<?> type, String name, Object[] args, boolean requireStatic) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (!method.getName().equals(name) || method.getParameterCount() != args.length
                        || (requireStatic && !Modifier.isStatic(method.getModifiers()))) continue;
                Class<?>[] types = method.getParameterTypes();
                boolean matches = true;
                for (int i = 0; i < types.length; i++) {
                    if (args[i] != null && !box(types[i]).isInstance(args[i])) { matches = false; break; }
                }
                if (matches) return method;
            }
        }
        throw new IllegalStateException(new NoSuchMethodException(type.getName() + '#' + name));
    }

    private static Class<?> box(Class<?> type) {
        if (!type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == char.class) return Character.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        return Void.class;
    }
}
