package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.DexResolver;
import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;
import android.view.View;

import com.shatyuka.zhiliao.xposed.XC_MethodHook;
import com.shatyuka.zhiliao.xposed.XposedBridge;
import com.shatyuka.zhiliao.xposed.XposedHelpers;

public class LaunchAd implements IHook {
    static Class<?> AdNetworkManager;

    static Method isShowLaunchAd;
    static Method requestLaunchAd;
    static Method requestLaunchFlow;
    static Method continueLaunch;

    @Override
    public String getName() {
        return "去启动页广告";
    }

    @Override
    public void init(final ClassLoader classLoader) throws Throwable {
        AdNetworkManager = Helper.findClass(classLoader, "com.zhihu.android.sdk.launchad.", 0, 28,
                (Class<?> clazz) -> clazz.getDeclaredField("c").getType().getName().startsWith("okhttp3."));
        if (AdNetworkManager == null) {
            throw new ClassNotFoundException("com.zhihu.android.sdk.launchad.AdNetworkManager");
        }
        Class<?> LaunchAdInterface = classLoader.loadClass("com.zhihu.android.ad.LaunchAdInterface");
        Helper.findClass(classLoader, "com.zhihu.android.app.util.", 0, 28,
                (Class<?> LaunchAdHelper) -> {
                    if (!LaunchAdInterface.isAssignableFrom(LaunchAdHelper) || LaunchAdHelper.isInterface())
                        return false;
                    isShowLaunchAd = LaunchAdHelper.getMethod("isShowLaunchAd");
                    return true;
                });
        if (isShowLaunchAd == null)
            isShowLaunchAd = DexResolver.findMethod("launch_ad_visibility",
                    "com.zhihu.android.app.util", LaunchAdInterface,
                    "isShowLaunchAd", boolean.class);
        if (isShowLaunchAd == null)
            throw new NoSuchMethodException("com.zhihu.android.app.util.LaunchAdHelper.isShowLaunchAd()");

        requestLaunchAd = Helper.getMethodByParameterTypes(AdNetworkManager,
                int.class, long.class, long.class, String.class);
        if (requestLaunchAd == null)
            throw new NoSuchMethodException("AdNetworkManager.requestLaunchAd(int,long,long,String)");

        // Zhihu 11.4 keeps the splash visible while this ad flow waits and later
        // budgets roughly 2600 ms for presentation. Calling its completion callback
        // skips only that launch-ad gate; Application initialization remains intact.
        try {
            Class<?> callback = classLoader.loadClass("com.zhihu.android.s1.a.a.b");
            Class<?> launchImpl = classLoader.loadClass("com.zhihu.android.launch.impl.LaunchImpl");
            requestLaunchFlow = launchImpl.getMethod("requestAd", String.class, callback, View.class);
            continueLaunch = callback.getMethod("b");
        } catch (Throwable ignored) {
            requestLaunchFlow = null;
            continueLaunch = null;
        }
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookMethod(isShowLaunchAd, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_launchad", true))
                    param.setResult(false);
            }
        });
        XposedBridge.hookMethod(requestLaunchAd, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_launchad", true)) {
                    param.setResult("");
                }
            }
        });
        if (requestLaunchFlow != null && continueLaunch != null) {
            XposedBridge.hookMethod(requestLaunchFlow, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    if (Helper.prefs.getBoolean("switch_mainswitch", false)
                            && Helper.prefs.getBoolean("switch_launch_optimize", false)) {
                        continueLaunch.invoke(param.args[1]);
                        param.setResult(null);
                    }
                }
            });
        }
    }
}
