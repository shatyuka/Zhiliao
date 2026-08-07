package com.shatyuka.zhiliao;

import android.content.SharedPreferences;

import org.luckypray.dexkit.DexKitBridge;
import org.luckypray.dexkit.query.FindClass;
import org.luckypray.dexkit.query.FindMethod;
import org.luckypray.dexkit.query.matchers.ClassMatcher;
import org.luckypray.dexkit.query.matchers.MethodMatcher;
import org.luckypray.dexkit.result.ClassData;
import org.luckypray.dexkit.result.MethodData;

import java.lang.reflect.Method;
import java.util.List;

import com.shatyuka.zhiliao.xposed.XposedBridge;

/** Runtime fallback for targets whose obfuscated names move between Zhihu releases. */
public final class DexResolver {
    private static final String CACHE_PREFIX = "dexkit_resolver_v1_";

    private static DexKitBridge bridge;
    private static ClassLoader classLoader;
    private static SharedPreferences preferences;
    private static String versionPrefix;

    private DexResolver() {
    }

    public static synchronized void open(String apkPath, ClassLoader loader,
                                         SharedPreferences prefs, int versionCode) {
        close();
        classLoader = loader;
        preferences = prefs;
        versionPrefix = CACHE_PREFIX + versionCode + "_";
        if (apkPath == null || apkPath.isEmpty())
            return;
        try {
            System.loadLibrary("dexkit");
            bridge = DexKitBridge.create(apkPath);
        } catch (Throwable throwable) {
            bridge = null;
            XposedBridge.log("[Zhiliao] DexKit unavailable: " + throwable);
        }
    }

    public static synchronized void close() {
        if (bridge != null) {
            try {
                bridge.close();
            } catch (Throwable ignored) {
            }
        }
        bridge = null;
        classLoader = null;
        preferences = null;
        versionPrefix = null;
    }

    public static synchronized Class<?> findClassBySuper(String key, String searchPackage,
                                                          Class<?> superClass) {
        Class<?> cached = loadCachedClass(key);
        if (cached != null && cached.getSuperclass() == superClass)
            return cached;
        if (bridge == null || classLoader == null)
            return null;
        try {
            List<ClassData> result = bridge.findClass(FindClass.create()
                    .searchPackages(searchPackage)
                    .matcher(ClassMatcher.create().superClass(superClass.getName())));
            for (ClassData data : result) {
                Class<?> candidate = data.getInstance(classLoader);
                if (candidate.getSuperclass() == superClass) {
                    save(key, candidate.getName());
                    return candidate;
                }
            }
        } catch (Throwable throwable) {
            XposedBridge.log("[Zhiliao] DexKit class query " + key + " failed: " + throwable);
        }
        return null;
    }

    public static synchronized Method findMethod(String key, String searchPackage,
                                                  Class<?> ownerType, String methodName,
                                                  Class<?> returnType, Class<?>... parameterTypes) {
        Method cached = loadCachedMethod(key, ownerType, returnType, parameterTypes);
        if (cached != null)
            return cached;
        if (bridge == null || classLoader == null)
            return null;
        try {
            String[] parameterTypeNames = new String[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++)
                parameterTypeNames[i] = parameterTypes[i].getName();
            List<MethodData> result = bridge.findMethod(FindMethod.create()
                    .searchPackages(searchPackage)
                    .matcher(MethodMatcher.create()
                            .name(methodName)
                            .returnType(returnType.getName())
                            .paramTypes(parameterTypeNames)));
            for (MethodData data : result) {
                Method candidate = data.getMethodInstance(classLoader);
                if ((ownerType == null || ownerType.isAssignableFrom(candidate.getDeclaringClass()))
                        && candidate.getReturnType() == returnType) {
                    save(key, candidate.getDeclaringClass().getName() + "#" + candidate.getName());
                    return candidate;
                }
            }
        } catch (Throwable throwable) {
            XposedBridge.log("[Zhiliao] DexKit method query " + key + " failed: " + throwable);
        }
        return null;
    }

    public static synchronized Class<?> findClassByMethodName(String key, String searchPackage,
                                                               String methodName) {
        Class<?> cached = loadCachedClass(key);
        if (cached != null)
            return cached;
        if (bridge == null || classLoader == null)
            return null;
        try {
            List<MethodData> result = bridge.findMethod(FindMethod.create()
                    .searchPackages(searchPackage)
                    .matcher(MethodMatcher.create().name(methodName)));
            for (MethodData data : result) {
                Method method = data.getMethodInstance(classLoader);
                Class<?> candidate = method.getDeclaringClass();
                save(key, candidate.getName());
                return candidate;
            }
        } catch (Throwable throwable) {
            XposedBridge.log("[Zhiliao] DexKit method-owner query " + key + " failed: " + throwable);
        }
        return null;
    }

    private static Class<?> loadCachedClass(String key) {
        String value = load(key);
        if (value == null || value.indexOf('#') >= 0 || classLoader == null)
            return null;
        try {
            return classLoader.loadClass(value);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static Method loadCachedMethod(String key, Class<?> ownerType, Class<?> returnType,
                                           Class<?>[] parameterTypes) {
        String value = load(key);
        if (value == null || classLoader == null)
            return null;
        int separator = value.lastIndexOf('#');
        if (separator <= 0)
            return null;
        try {
            Class<?> owner = classLoader.loadClass(value.substring(0, separator));
            if (ownerType != null && !ownerType.isAssignableFrom(owner))
                return null;
            Method method = owner.getMethod(value.substring(separator + 1), parameterTypes);
            return method.getReturnType() == returnType ? method : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String load(String key) {
        return preferences == null || versionPrefix == null
                ? null : preferences.getString(versionPrefix + key, null);
    }

    private static void save(String key, String value) {
        if (preferences != null && versionPrefix != null)
            preferences.edit().putString(versionPrefix + key, value).apply();
    }
}
