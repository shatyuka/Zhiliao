package com.shatyuka.zhiliao.xposed;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;
import java.util.Set;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;

public final class XposedBridge {
    private static XposedModule module;
    private XposedBridge() {}

    public static void attach(XposedModule value) { module = value; }

    public static XC_MethodHook.Unhook hookMethod(Executable executable, XC_MethodHook callback) {
        if (module == null) throw new IllegalStateException("libxposed module is not attached");
        executable.setAccessible(true);
        XposedInterface.HookHandle handle = module.hook(executable)
                .setExceptionMode(XposedInterface.ExceptionMode.PROTECTIVE)
                .intercept(callback::intercept);
        return callback.newUnhook(handle);
    }

    public static Set<XC_MethodHook.Unhook> hookAllMethods(Class<?> type, String name, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> result = new LinkedHashSet<>();
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(name) && !Modifier.isAbstract(method.getModifiers()))
                    result.add(hookMethod(method, callback));
            }
        }
        return result;
    }

    public static Set<XC_MethodHook.Unhook> hookAllConstructors(Class<?> type, XC_MethodHook callback) {
        Set<XC_MethodHook.Unhook> result = new LinkedHashSet<>();
        for (Constructor<?> constructor : type.getDeclaredConstructors())
            result.add(hookMethod(constructor, callback));
        return result;
    }

    public static void log(String message) {
        if (module != null) module.log(android.util.Log.INFO, "Zhiliao", message);
        else android.util.Log.i("Zhiliao", message);
    }
}
