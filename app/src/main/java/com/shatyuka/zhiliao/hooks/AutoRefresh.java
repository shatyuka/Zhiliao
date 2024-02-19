package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;
import java.util.Arrays;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class AutoRefresh implements IHook {

    static Method feedAutoRefreshManager_shouldRefresh;

    static Method feedHotRefreshAbConfig_shouldRefresh;

    @Override
    public String getName() {
        return "关闭首页自动刷新";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        Class<?> feedAutoRefreshManager = classLoader.loadClass("com.zhihu.android.app.feed.util.j");

        feedAutoRefreshManager_shouldRefresh = Arrays.stream(feedAutoRefreshManager.getDeclaredMethods())
                .filter(method -> method.getReturnType() == void.class)
                .filter(method -> method.getParameterCount() == 4)
                .filter(method -> {
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    return parameterTypes[0] == long.class && parameterTypes[1] == int.class;
                }).findFirst().get();

        Class<?> feedHotRefreshAbConfig = classLoader.loadClass("com.zhihu.android.app.feed.util.p");

        feedHotRefreshAbConfig_shouldRefresh = Arrays.stream(feedHotRefreshAbConfig.getDeclaredMethods())
                .filter(method -> method.getReturnType() == boolean.class)
                .filter(method -> method.getParameterCount() == 1)
                .filter(method -> method.getParameterTypes()[0] == long.class)
                .findFirst().get();

    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(feedAutoRefreshManager_shouldRefresh, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)
                        && Helper.prefs.getBoolean("switch_autorefresh", true)) {
                    param.args[0] = 0;
                }
            }
        });

        XposedBridge.hookMethod(feedHotRefreshAbConfig_shouldRefresh, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                if (Helper.prefs.getBoolean("switch_mainswitch", false)
                        && Helper.prefs.getBoolean("switch_autorefresh", true)) {
                    param.setResult(false);
                }
            }
        });

    }
}
