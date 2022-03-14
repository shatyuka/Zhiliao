package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import java.lang.reflect.Method;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class LaunchAd implements IHook {
    static Class<?> AdNetworkManager;

    static Method isShowLaunchAd;

    @Override
    public String getName() {
        return "去启动页广告";
    }

    @Override
    public void init(final ClassLoader classLoader) throws Throwable {
        AdNetworkManager = classLoader.loadClass("com.zhihu.android.sdk.launchad.b");
        Helper.findClass(classLoader, "com.zhihu.android.app.util.", 3, 2,
                (Class<?> LaunchAdHelper) -> {
                    isShowLaunchAd = LaunchAdHelper.getMethod("isShowLaunchAd");
                    return true;
                });
        if (isShowLaunchAd == null)
            throw new NoSuchMethodException("com.zhihu.android.app.util.LaunchAdHelper.isShowLaunchAd()");
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
        XposedHelpers.findAndHookMethod(AdNetworkManager, "a", int.class, long.class, long.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_launchad", true)) {
                    param.setResult("");
                }
            }
        });
    }
}
