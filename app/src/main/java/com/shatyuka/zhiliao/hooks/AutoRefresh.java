package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicBoolean;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class AutoRefresh implements IHook {
    static Method returnTopOrRefresh;
    static Method returnTopAndRefresh;
    static Method tryRefresh;
    static Method fetchFeed;
    static Method ui2_refresh;
    static Method ui2_refreshWithType;
    static Method ui2_postRefreshSucceed;

    static Field Params_nextUrl;

    boolean refreshSucceed;

    @Override
    public String getName() {
        return "禁止自动刷新";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        try {
            Class<?> BaseTabChildFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui.fragment.BaseTabChildFragment");
            try {
                returnTopOrRefresh = BaseTabChildFragment.getMethod("a", boolean.class);
                returnTopAndRefresh = BaseTabChildFragment.getMethod("b", boolean.class);
            } catch (NoSuchMethodException ignored) {
                try {
                    returnTopOrRefresh = BaseTabChildFragment.getMethod("returnTopOrRefresh", boolean.class);
                    returnTopAndRefresh = BaseTabChildFragment.getMethod("returnTopAndRefresh", boolean.class);
                } catch (NoSuchMethodException ignored2) {
                }
            }
        } catch (ClassNotFoundException ignored) {
        }

        try {
            Class<?> FeedAutoRefreshManager = classLoader.loadClass("com.zhihu.android.app.feed.util.FeedAutoRefreshManager");
            findTryRefreshMethod(FeedAutoRefreshManager);
        } catch (ClassNotFoundException ignored) {
            Helper.findClass(classLoader, "com.zhihu.android.app.feed.util.", 0, 1, this::findTryRefreshMethod);
        }

        Helper.findClass(classLoader, "com.zhihu.android.app.feed.ui2.feed.a.", 0, 1,
                (Class<?> clazz) -> {
                    if (clazz.getDeclaredField("c").getType() == AtomicBoolean.class) {
                        Class<?> FeedRemoteDataSource = clazz.getDeclaredField("a").getType();
                        Method[] methods = FeedRemoteDataSource.getMethods();
                        for (Method method : methods) {
                            if ((method.getParameterCount() == 2 && method.getParameterTypes()[1] != Object.class) ||
                                    (method.getParameterCount() == 3 && method.getParameterTypes()[2] == boolean.class)) {
                                Class<?> FeedParams = method.getParameterTypes()[0];
                                Params_nextUrl = FeedParams.getDeclaredField("f");
                                Params_nextUrl.setAccessible(true);
                                fetchFeed = method;
                                return true;
                            }
                        }
                    }
                    return false;
                });

        try {
            Class<?> FeedFragment = classLoader.loadClass("com.zhihu.android.app.feed.ui2.feed.FeedFragment");
            try {
                ui2_refresh = FeedFragment.getDeclaredMethod("refresh", boolean.class);
            } catch (NoSuchMethodException ignored) {
            }
            Method[] methods = FeedFragment.getMethods();
            for (Method method : methods) {
                if (method.getName().equals("a") && method.getParameterCount() == 2 && method.getParameterTypes()[0] == boolean.class) {
                    ui2_refreshWithType = method;
                    break;
                }
            }
            try {
                Class<?> ZHObjectList = classLoader.loadClass("com.zhihu.android.api.model.ZHObjectList");
                ui2_postRefreshSucceed = FeedFragment.getDeclaredMethod("postRefreshSucceed", ZHObjectList);
            } catch (NoSuchMethodException ignored) {
            }
        } catch (ClassNotFoundException ignored) {
        }
    }

    @Override
    public void hook() throws Throwable {
        if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_autorefresh", false)) {
            if (returnTopOrRefresh != null) {
                XposedBridge.hookMethod(returnTopOrRefresh, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        if ((boolean) param.args[0]) {
                            param.setResult(1);
                        }
                    }
                });
            }
            if (returnTopAndRefresh != null) {
                XposedBridge.hookMethod(returnTopAndRefresh, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(null);
                    }
                });
            }
            if (tryRefresh != null) {
                XposedBridge.hookMethod(tryRefresh, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.setResult(null);
                    }
                });
            }
            if (fetchFeed != null) {
                XposedBridge.hookMethod(fetchFeed, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Object params = param.args[0];
                        String nextUrl = (String) Params_nextUrl.get(params);
                        if (nextUrl == null) {
                            param.setResult(null);
                        }
                    }
                });
            }
            if (ui2_refresh != null) {
                XposedBridge.hookMethod(ui2_refresh, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        param.args[0] = true;
                    }
                });
            }
            if (ui2_refreshWithType != null) {
                XposedBridge.hookMethod(ui2_refreshWithType, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) {
                        refreshSucceed = false;
                    }
                });
            }
            if (ui2_postRefreshSucceed != null) {
                XposedBridge.hookMethod(ui2_postRefreshSucceed, new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        if (refreshSucceed) {
                            param.setResult(null);
                        } else {
                            refreshSucceed = true;
                        }
                    }
                });
            }
        }
    }

    private boolean findTryRefreshMethod(Class<?> clazz) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getReturnType() == void.class && method.getParameterCount() == 4 &&
                    method.getParameterTypes()[0] == long.class &&
                    method.getParameterTypes()[1] == int.class &&
                    (method.getParameterTypes()[2].getModifiers() & Modifier.INTERFACE) != 0 &&
                    (method.getParameterTypes()[3].getModifiers() & Modifier.ABSTRACT) != 0
            ) {
                tryRefresh = method;
                return true;
            }
        }
        return false;
    }
}
