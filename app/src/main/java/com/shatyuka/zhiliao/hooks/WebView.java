package com.shatyuka.zhiliao.hooks;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class WebView implements IHook {
    @Override
    public String getName() {
        return "WebView调试";
    }

    @Override
    public void init(ClassLoader classLoader) throws Throwable {

    }

    @Override
    public void hook() throws Throwable {
        XposedBridge.hookAllConstructors(android.webkit.WebView.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedHelpers.callStaticMethod(android.webkit.WebView.class, "setWebContentsDebuggingEnabled", true);
            }
        });
    }
}
