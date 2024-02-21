package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

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
        feedAutoRefreshManager_shouldRefresh = findFeedAutoRefreshManagerShouldRefreshMethod(classLoader);
        feedHotRefreshAbConfig_shouldRefresh = findFeedHotRefreshAbConfigShouldRefreshMethod(classLoader);
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

    private Method findFeedHotRefreshAbConfigShouldRefreshMethod(ClassLoader classLoader) throws NoSuchMethodException {
        List<String> feedHotRefreshAbConfigClassNameList = Arrays.asList("com.zhihu.android.app.feed.util.p1",
                "com.zhihu.android.app.feed.util.p");

        for (String className : feedHotRefreshAbConfigClassNameList) {
            try {
                Class<?> feedHotRefreshAbConfig = classLoader.loadClass(className);
                return Arrays.stream(feedHotRefreshAbConfig.getDeclaredMethods())
                        .filter(method -> method.getReturnType() == boolean.class)
                        .filter(method -> method.getParameterCount() == 1)
                        .filter(method -> method.getParameterTypes()[0] == long.class)
                        .findFirst().get();
            } catch (Exception ignore) {
            }
        }

        throw new NoSuchMethodException("FeedHotRefreshAbConfig#shouldRefresh");
    }

    private Method findFeedAutoRefreshManagerShouldRefreshMethod(ClassLoader classLoader) throws NoSuchMethodException {
        List<String> feedAutoRefreshManagerClassNameList = Arrays.asList("com.zhihu.android.app.feed.util.FeedAutoRefreshManager",
                "com.zhihu.android.app.feed.util.j");

        for (String className : feedAutoRefreshManagerClassNameList) {
            try {
                Class<?> feedAutoRefreshManager = classLoader.loadClass(className);
                return Arrays.stream(feedAutoRefreshManager.getDeclaredMethods())
                        .filter(method -> method.getReturnType() == void.class)
                        .filter(method -> method.getParameterCount() == 4)
                        .filter(method -> {
                            Class<?>[] parameterTypes = method.getParameterTypes();
                            return parameterTypes[0] == long.class && parameterTypes[1] == int.class;
                        }).findFirst().get();
            } catch (Exception ignore) {
            }
        }

        throw new NoSuchMethodException("FeedAutoRefreshManager#shouldRefresh");
    }

}
