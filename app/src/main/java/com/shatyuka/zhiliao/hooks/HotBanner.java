package com.shatyuka.zhiliao.hooks;

import com.shatyuka.zhiliao.Helper;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class HotBanner implements IHook {
    static Class<?> InternalNotificationManager;

    @Override
    public String getName() {
        return "隐藏热点通知";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {
        InternalNotificationManager = classLoader.loadClass("com.zhihu.android.app.feed.notification.InternalNotificationManager");
    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookAllMethods(InternalNotificationManager, "fetchFloatNotification", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) {
                if (Helper.prefs.getBoolean("switch_mainswitch", false) && Helper.prefs.getBoolean("switch_hotbanner", false)) {
                    param.setResult(null);
                }
            }
        });
    }
}
